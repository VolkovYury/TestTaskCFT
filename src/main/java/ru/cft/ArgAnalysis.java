package ru.cft;

import java.util.ArrayList;

import org.apache.commons.cli.*;

public class ArgAnalysis {

    static ArrayList<String> argFilesIO = new ArrayList<String>();

    /**
     * Функция анализирует аргументы командной строки для выбора режима работы программы
     * @param args Аргументы командной строки
     */
    void parseParameters(String[] args) {
        // https://commons.apache.org/proper/commons-cli/usage.html
        Options options = new Options();

        // Определение трёх групп - справочная группа, режим сортировки, тип сортируемых файлов
        OptionGroup group0 = new OptionGroup();
        group0.addOption(new Option("h", "help", false, "Справка"));
        options.addOptionGroup(group0);

        OptionGroup group1 = new OptionGroup();
        group1.addOption(new Option("a", "ascendingSort", false, "Сортировка по возрастанию ()"));
        group1.addOption(new Option("d", "descendingSort", false, "Сортировка по убыванию"));
        options.addOptionGroup(group1);

        OptionGroup group2 = new OptionGroup();
        group2.addOption(new Option("i", "integer", false, "Сортировка целых чисел"));
        group2.addOption(new Option("s", "string", false, "Сортировка строк"));
        group2.setRequired(true);
        options.addOptionGroup(group2);

        CommandLineParser parser = new DefaultParser();
        CommandLine commands = null;

        try {
            commands = parser.parse(options, args);
        } catch (UnrecognizedOptionException e) {
            System.out.println("Передана неизвестная опция - " + e.getOption());
            printHelp(options, 100);
        } catch (MissingOptionException e) {
            System.out.println("Не обнаружены обязательные параметры: i или s");
            printHelp(options, 101);
        } catch (ParseException e) {
            System.out.println("Ошибка при поиске аргументов командной строки");
            printHelp(options, 102);
        }

        if (commands != null) {
            // Вывод справки. Необходим обязательный параметр, иначе убрать одну из веток catch (MissingOptionException)
            if (commands.hasOption("h")) {
                printHelp(options, 0);
            }

            // Проверка конфликтов среди параметров
            if (commands.hasOption("a") && commands.hasOption("d")) {
                System.out.println("Введены конфликтующие опции (a и d)");
                printHelp(options, 103);
            }
            if (commands.hasOption("i") && commands.hasOption("s")) {
                System.out.println("Введены конфликтующие опции (i и s)");
                printHelp(options, 104);
            }

            // Проверка наличия обязательного параметра.
            // Ошибка отлавливается через catch. Для полноты картины оставил эту проверку
            if (!commands.hasOption("i") && !commands.hasOption("s")) {
                System.out.println("Отсутствует обязательная опция (i или s)");
                printHelp(options, 101);
            }

            ArrayList<String> filesIO = filenameAnalysis(commands.getArgs());
            if (filesIO.size() < 2) {
                System.out.println("Не обнаружен минимальный набор входных и выходных данных (2 и более файла)");
                filesIO.clear();
                printHelp(options, 105);
            }

            if (commands.hasOption("s")) {
                Main.isString = true;
                System.out.println("Выбран режим сортировки целых чисел");
            } else {
                System.out.println("Выбран режим сортировки строк");
            }

            if (commands.hasOption("d")) {
                Main.descendingSort = true;
                System.out.println("Выбран режим сортировки по убыванию");
            } else {
                System.out.println("Выбран режим сортировки по возрастанию");
            }

            argFilesIO = filesIO;

        } else {
            System.out.println("Непредвиденная ошибка");
            printHelp(options, -1);
        }
    }

    /**
     * Функция выводит справочную информацию в консоль и закрывает программу с переданным кодом ошибки
     * @param options Перечень определённых опций
     * @param errCode Код ошибки завершения
     */
    private static void printHelp(Options options, int errCode) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("""
                MergeSort.jar [OPTIONS] out.txt in.txt
                out.txt - имя файла в формате .txt с результатом сортировки (обязательный параметр).
                in.txt - имена входных файлов, не менее одного (обязательный параметр).
                """, options);
        System.exit(errCode);
    }

    /**
     * Функция анализирует аргументы командной строки для поиска названия (путей) обрабатываемых файлов
     * @param args Аргументы командной строки
     * @return Функция возвращает ArrayList с названиями обрабатываемых файлов (названия или полные пути). Элемент с
     * индексом 0 - выходной файл (результат работы всей сортировки)
     */
    private static ArrayList<String> filenameAnalysis(String[] args) {
        ArrayList<String> files = new ArrayList<>();
        char[] formatFile = ".txt".toCharArray();

        // Поиск среди аргументов командной строки строк, соответствующих маске "*.txt"
        for (String arg : args) {
            char[] param = arg.toCharArray();

            if (param[0] != '-' && param.length > formatFile.length) {
                for (int i = 0, k = 0; i < param.length; i++) {

                    if (param[i] == formatFile[k])
                        k++;
                    else
                        k = 0;

                    if (k == 4 && i == (param.length - 1))
                        files.add(arg);
                }
            }
        }

        return files;
    }

}
