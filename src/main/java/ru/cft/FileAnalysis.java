package ru.cft;

import java.io.*;
import java.util.*;

public class FileAnalysis {

    private static ArrayList<String> filesIOFinal = new ArrayList<>();
    static ArrayList<ArrayList<String>> data = new ArrayList<>();
    static Scanner sc;

    /**
     * Функция осуществляет первичную обработку списка файлов (создание/чтение/запись)
     * @param files ArrayList с названиями обрабатываемых файлов (названия или полные пути)
     * @return ArrayList доступных для обработки файлов
     */
    private static ArrayList<String> findFiles(ArrayList<String> files) {

        // Поиск/создание выходного файла
        String absoluteFilePathOut = files.get(0);
        File fileOutput = new File(absoluteFilePathOut);
        try {
            if(fileOutput.createNewFile()){
                System.out.println("Файл "+absoluteFilePathOut+" успешно создан");
            } else {
                System.out.println("Файл " + absoluteFilePathOut + " уже существует");

                if (fileOutput.canWrite()) {
                    System.out.println("Доступ на запись в файл есть. Информация будет перезаписана");
                } else {
                    System.out.println("Доступ на запись в файл отсутствует");
                    files.clear();
                    System.exit(200);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Поиск входных файлов
        for (int i = 1; i < files.size();) {
            String absoluteFilePathIn = files.get(i);
            File fileInput = new File(absoluteFilePathIn);

            if (fileInput.exists()) {
                System.out.println("Файл " + absoluteFilePathIn + " успешно обнаружен");

                if (fileInput.canRead()) {
                    System.out.println("Доступ на чтение из файла есть");
                    i++;
                } else {
                    System.out.println("Доступ на чтение из файла отсутствует. Файл будет удалён из списка");
                    files.remove(i);
                }
            } else {
                System.out.println("Файл " + absoluteFilePathIn + " не обнаружен. Файл будет удалён из списка");
                files.remove(i);
            }
        }

        if (files.size() < 2) {
            System.out.println("Не обнаружен минимальный набор доступных входных и выходных данных (2 и более файла)");
            files.clear();
            System.exit(201);
        }

        return files;
    }

    /**
     * Функция считывает содержимое файлов и проверяет содержимое
     * @param files ArrayList с исходными файлами
     * @return ArrayList с данными исходных файлов в виде ArrayList<String>
     */
    private static ArrayList<ArrayList<String>> readFile(ArrayList<String> files) {

        for (int i = 1; i < files.size(); i++) {
            ArrayList<String> arr = new ArrayList<>();
            boolean flag = true;

            try {
                FileInputStream inputStream = new FileInputStream(files.get(i));
                sc = new Scanner(inputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            // Цикл построчного чтения
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line == null)
                    continue;

                // Проверки. Для строк запрещены пробелы, для целых чисел - нечисловые данные
                if (Main.isString) {
                    if (line.indexOf(" ") > 0) {
                        System.out.println("Найдены недопустимые данные: " + line);
                        System.out.println("Данные файла " + files.get(i) + " отсортированы не будут");
                        flag = false;
                        break;
                    }
                } else {

                    try {
                        int result = Integer.parseInt(line);
                    } catch (NumberFormatException e) {
                        System.out.println("Найдены недопустимые данные: " + line);
                        System.out.println("Данные файла " + files.get(i) + " отсортированы не будут");
                        flag = false;
                        break;
                    }

                }

                arr.add(line);
            }

            // Дополнительная проверка сортировки каждого файла. Некорректно отсортированные данные будут отброшены
            correctSort(arr, files.get(i));

            if (flag)
                data.add(arr);
        }

        if (data.size() < 1) {
            System.out.println("Не обнаружен минимальный набор доступных входных данных (1 или более файлов)");
            System.exit(202);
        }

        return data;
    }

    /**
     * Функция считывает содержимое файлов, проверяет порядок сортировки и отсекает неотсортированные части
     * @param arrayData ArrayList с исходными файлами в проверенном формате
     * @param filename имя файла, содержимое которого проверяется. Необходим только для справочной информации
     */
    private static void correctSort(ArrayList<String> arrayData, String filename) {

        if (arrayData.size() < 2) {
            return;
        }

        for (int i = 0; i < arrayData.size()-1; i++) {
            boolean flag = false;

            if (Main.isString) {
                int result = arrayData.get(i).compareTo(arrayData.get(i+1));

                if (Main.descendingSort) {
                    if ( result < 0) {
                        flag = true;
                    }
                } else {
                    if ( result >= 0) {
                        flag = true;
                    }
                }

            } else {
                int firstValue = Integer.parseInt(arrayData.get(i));
                int secondValue = Integer.parseInt(arrayData.get(i+1));

                if (Main.descendingSort) {
                    if (secondValue > firstValue) {
                        flag = true;
                    }
                } else {
                    if (secondValue < firstValue) {
                        flag = true;
                    }
                }
            }

            // Уничтожение неподходящих данных
            if (flag) {
                System.out.println("Обнаружено нарушение сортировки в файле : " + filename);
                System.out.println("Часть данных не будет включена в отсортированный список (начиная с " + ++i + " строки)");

                while (i < arrayData.size()) {
                    arrayData.remove(i);
                }

                return;
            }
        }
    }

    /**
     * Функция слияния отсортированных массивов
     * @param arr ArrayList с данными, извлечёнными из файлов
     */
    void merge(ArrayList<ArrayList<String>> arr) {
        filesIOFinal = findFiles(ArgAnalysis.argFilesIO);
        data = readFile(filesIOFinal);

        // Все "массивы" данных начиная с последних постепенно вольются в один
        while (arr.size() > 1) {

            int arrSize = arr.size();
            ArrayList<String> tmp = new ArrayList<>();

            // Слияние до тех пор, пока оба массива имеют данные. Заатем к результату присоединяется остаток
            while (arr.get(arrSize-1).size() > 0 && arr.get(arrSize-2).size() > 0) {

                String var1str = arr.get(arrSize-1).get(0);
                String var2str = arr.get(arrSize-2).get(0);

                if (Main.isString) {

                    int result = var1str.compareTo(var2str);

                    if (Main.descendingSort) {
                        if ( result >= 0) {
                            tmp.add(var1str);
                            arr.get(arrSize-1).remove(0);
                        } else {
                            tmp.add(var2str);
                            arr.get(arrSize-2).remove(0);
                        }
                    } else {
                        if ( result <= 0) {
                            tmp.add(var1str);
                            arr.get(arrSize-1).remove(0);
                        } else {
                            tmp.add(var2str);
                            arr.get(arrSize-2).remove(0);
                        }
                    }

                } else {

                    int var1 = Integer.parseInt(var1str);
                    int var2 = Integer.parseInt(var2str);

                    if (Main.descendingSort) {

                        if ( var1 >= var2) {
                            tmp.add(var1str);
                            arr.get(arrSize-1).remove(0);
                        } else {
                            tmp.add(var2str);
                            arr.get(arrSize-2).remove(0);
                        }

                    } else {

                        if ( var1 <= var2) {
                            tmp.add(var1str);
                            arr.get(arrSize-1).remove(0);
                        } else {
                            tmp.add(var2str);
                            arr.get(arrSize-2).remove(0);
                        }
                    }

                }

            }

            while (arr.get(arrSize-1).size() > 0) {
                tmp.add(arr.get(arrSize-1).get(0));
                arr.get(arrSize-1).remove(0);
            }

            while (arr.get(arrSize-2).size() > 0) {
                tmp.add(arr.get(arrSize-2).get(0));
                arr.get(arrSize-2).remove(0);
            }

            arr.set(arrSize-2, tmp);
            arr.remove(arr.size()-1);
        }

        writeFile(arr.get(0), filesIOFinal.get(0));
    }

    /**
     * Функция записи данных сортировки в выходной файл
     * @param result ArrayList с результатом сортировки
     * @param filename Путь к файлу
     */
    private static void writeFile(ArrayList<String> result, String filename) {
        File file = new File(filename);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            for (String s : result) {
                fileOutputStream.write(s.getBytes(), 0, s.length());
                fileOutputStream.write("\n".getBytes());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


