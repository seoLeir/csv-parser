package io.seoleir.util;

import lombok.extern.slf4j.Slf4j;
import io.seoleir.model.MNPModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CSVParser {

    public static List<MNPModel> readLineByLine(BufferedReader bufferedReader) {
        List<MNPModel> records = new ArrayList<>();
        try (bufferedReader) {
            String line;
            boolean first = true;

            while ((line = bufferedReader.readLine()) != null) {
                if(first) {
                    first = false;
                    continue;
                }

                String[] values = line.split(";");

                records.add(MNPModel.builder()
                        .number(valueChecker(values, 0))
                        .owner(valueChecker(values, 1))
                        .rn(valueChecker(values, 2))
                        .mnc(valueChecker(values, 3))
                        .portDate(valueChecker(values, 4))
                        .rowCount(valueChecker(values, 5))
                        .build());
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return records;
    }

    private static String valueChecker(String[] array, Integer index) {
        return array.length > index && array[index] != null ? array[index] : null;
    }

}
