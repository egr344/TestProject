package ru.parser;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {

        try {
            String baseUrl = "https://usedhp.ru";
            String firstPageCPU = "https://usedhp.ru/magazin/folder/protsessory?view=list";
            String firstPageRAM = "https://usedhp.ru/magazin/folder/operativnaya-pamyat?view=list";
            String firstPageHDD = "https://usedhp.ru/magazin/folder/zhestkie-diski?view=list";
            String firstPageDiskSlade = "https://usedhp.ru/magazin/folder/salazki-dlya-diskov?view=list";
            String fistPageRaid = "https://usedhp.ru/magazin/folder/raid-kontrollery?view=list";
            Map<String, String> cpu = getMapModelByPrice(baseUrl, firstPageCPU);
            Map<String, String> ram = getMapModelByPrice(baseUrl, firstPageRAM);
            Map<String, String> hdd = getMapModelByPrice(baseUrl, firstPageHDD);
            Map<String, String> diskSlade = getMapModelByPrice(baseUrl, firstPageDiskSlade);
            Map<String, String> raid = getMapModelByPrice(baseUrl, fistPageRaid).entrySet().stream()
                    .filter(set -> !set.getKey().contains("Кэш"))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            Workbook workbook = new HSSFWorkbook();
            Sheet sheet = workbook.createSheet("usedHP");
            int startIndex = 0;
            startIndex = addInformationInXls(sheet, cpu, startIndex);
            startIndex = addInformationInXls(sheet, ram, startIndex);
            startIndex = addInformationInXls(sheet, hdd, startIndex);
            startIndex = addInformationInXls(sheet, diskSlade, startIndex);
            addInformationInXls(sheet, raid, startIndex);
            try (FileOutputStream fileOutputStream = new FileOutputStream("C:\\test\\test.xls")) {
                workbook.write(fileOutputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int addInformationInXls(Sheet sheet, Map<String, String> map, int startIndex) {
        for (Map.Entry<String, String> processor : map.entrySet()) {
            Row row = sheet.createRow(startIndex);
            Cell cell = row.createCell(0);
            cell.setCellValue(processor.getKey());
            Cell cell1 = row.createCell(1);
            cell1.setCellType(Cell.CELL_TYPE_NUMERIC);
            cell1.setCellValue(Long.parseLong(processor.getValue()));
            startIndex++;
        }
        return startIndex;
    }

    private static Map<String, String> getMapModelByPrice(String baseUrl, String firstPage) throws IOException {
        Map<String, String> modelToPrice = new HashMap<>();
        Document document = Jsoup.connect(firstPage).get();
        Element element = document.body();
        List<Element> nextPage;
        boolean isNextPageExist;
        do {
            List<Element> elementList = element.select("body > div.wrapper.editorElement.layer-type-wrapper > " +
                    "div.editorElement.layer-type-block.ui-droppable.block-9 > div.layout.column.layout_21 > div > " +
                    "div > article > div:nth-child(3) > form");
            for (Element raw : elementList) {
                String name = raw.select("div.shop2-product-price-left > div.name-column > " +
                        "div.product-wrapping > div.product-name > a").text();
                String price = raw.select("div.shop2-product-price-right > div.price-column > div")
                        .text().replace(" руб.", "").replace(" ", "");
                modelToPrice.put(name, price);
            }
            nextPage = element.select("body > div.wrapper.editorElement.layer-type-wrapper >" +
                    " div.editorElement.layer-type-block.ui-droppable.block-9 > div.layout.column.layout_21 > " +
                    "div > div > article > ul > li.page-next");
            isNextPageExist = !nextPage.isEmpty();
            if (isNextPageExist) {
                String path = nextPage.get(0).child(0).attributes().get("href");
                element = Jsoup.connect(baseUrl + path).get().body();
            }
        } while (isNextPageExist);
        return modelToPrice;
    }

}