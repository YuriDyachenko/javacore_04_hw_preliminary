package kurs;

import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Main {

    //глобальными оставляю только константы, "рука не поднимается" переменные тут объявлять :)

    //размер матрицы не желательно делать больше 99, вывод таблицы станет "некрасивым"
    //минимальный размер матрицы 1, ошибки не будет и с 0, но как тогда ввести нужные координаты?
    public static final int SIZE = 3;
    //число символов победы можно дедать и больше размера матрицы, тогда всегда должна быть ничья
    //минимальное число тут тоже может быть даже 1
    public static final int DOTS_TO_WIN = 3;

    public static final char DOT_EMPTY = ' ';
    public static final char DOT_X = 'X';
    public static final char DOT_O = 'O';
    //здесь, по идее, пригодился бы enum, но мы справимся пока без него
    public static final int DEAD_HEAD = 0;
    public static final int HUMAN = 1;
    public static final int AI = 2;
    public static final int BREAK = 3;
    //возможность играть ИИ против ИИ
    public static final boolean AI_VS_AI = false;
    //типы найденных последовательностей: ничего, строка, колонка, главная диагональ, побочная диагональ
    public static final int NONE = 0;
    public static final int ROW = 1;
    public static final int COLUMN = 2;
    public static final int MAIN_DIAG = 3;
    public static final int SIDE_DIAG = 4;
    //в массиве из двух координат 0 - x, 1 - y
    public static final int X = 0;
    public static final int Y = 1;

    public static void main(String[] args) {

        //так не хочется делать глобальные переменные (не константы),
        //хоть другой класс создавай, но мы такого не изучали еще
        //иначе все, что ниже объявляется, было бы полями класса Game, например

        //будем передавать сканер в тот метод, где потребуется ввод данных
        Scanner scanner = new Scanner(System.in);

        //для печати вспомогательная строка, чтобы не формировать ее каждый раз
        String horLine = createHorLine();

        //создаем и инициализируем матрицу
        char[][] map = createMap();

        //идея проверки победной последовательности такая - будет
        //текущий массив координат, накапливающий победные символы
        //вынесение победной последовательности координат в эту переменную
        //позволит при выводе матрицы обозначить те ячейки
        //которые "победили", например
        //+-----+-----+-----+-----+
        //|     | x=1 | x=2 | x=3 |
        //+-----+-----+-----+-----+
        //| y=1 |> X <|  O  |  O  |
        //+-----+-----+-----+-----+
        //| y=2 |> X <|     |  O  |
        //+-----+-----+-----+-----+
        //| y=3 |> X <|  X  |     |
        //+-----+-----+-----+-----+
        //он называется "победным", потому что мы считаем
        //что в этом массиве сохранилась именно победная последовательность
        //а если нет, то мы его почистим после каждой неудачной проверки
        int[][] coordinatesWin = createCoordinates();

        //пусть жребий решит, кто начинает первым игру
        int whoIsStarting = new Random().nextInt(2) + HUMAN;

        //выводим условия игры
        outGameHeader(whoIsStarting == AI);

        //все приходится передавать в метод параметрами
        printMap(map, horLine, coordinatesWin);

        //изначально - ничья
        int winner = DEAD_HEAD;

        do {

            //любой, кто начинает, начинает крестиками
            //если начинает человек
            if (whoIsStarting == HUMAN) {
                //возможность играть ИИ против ИИ
                if (AI_VS_AI) {
                    aiTurn(map, DOT_X);
                } else {
                    //если хотят выйти, получим "Вы сдались"
                    if (!humanTurn(map, scanner, DOT_X)) {
                        winner = BREAK;
                        break;
                    }
                }
            } else {
                aiTurn(map, DOT_X);
            }

            //чтобы отобразить победную строку/колонку/ряд
            //нужно сначала проверить, а потом печатать
            int hasWin = checkSome(map, coordinatesWin, DOT_X, DOTS_TO_WIN, null);
            printMap(map, horLine, coordinatesWin);
            if (hasWin != NONE) {
                winner = whoIsStarting;
                break;
            }

            //второй всегда играет ноликами
            if (whoIsStarting == HUMAN) {
                //АИ - все по аналогии, только захотеть выйти он не может
                aiTurn(map, DOT_O);
            } else {
                //возможность играть ИИ против ИИ
                if (AI_VS_AI) {
                    aiTurn(map, DOT_O);
                } else {
                    //если хотят выйти, получим "Вы сдались"
                    if (!humanTurn(map, scanner, DOT_O)) {
                        winner = BREAK;
                        break;
                    }
                }
            }

            hasWin = checkSome(map, coordinatesWin, DOT_O, DOTS_TO_WIN, null);
            printMap(map, horLine, coordinatesWin);
            if (hasWin != NONE) {
                winner = whoIsStarting == HUMAN ? AI : HUMAN;
                break;
            }

        } while (mapIsNotFull(map));

        //выводим победителя или ничью
        System.out.printf("%s\n", winner == HUMAN ? AI_VS_AI ? "За Вас победил другой ИИ! :)" : "Вы победили!" :
                winner == AI ? "Победил ИИ!" : winner == BREAK ? "Вы сдались... :(" : "Честная НИЧЬЯ!");

        scanner.close();
    }

    private static void outGameHeader(boolean thisIsAI) {
        System.out.printf("Играем в крестики-нолики (первый ход - %c)\n", DOT_X);
        System.out.printf("Размер матрицы: %dх%d, число %c|%c для выигрыша: %d\n",
                SIZE, SIZE, DOT_X, DOT_O, DOTS_TO_WIN);
        System.out.println(thisIsAI ? "Начинает ИИ..." : AI_VS_AI ? "За Вас начинает другой ИИ..." : "Вы начинаете...");
    }

    private static int[][] createCoordinates() {
        //в принципе, тут никогда не будет "длинее", чем нужно символов для победы
        int[][] coordinates = new int[DOTS_TO_WIN][2];
        //начальная очистка заполнением {-1, -1}
        clearCoordinates(coordinates, null);
        return coordinates;
    }

    private static String createHorLine() {
        //создание горизональной линии, чтобы не строить ее каждый раз
        StringBuilder stringBuilder = new StringBuilder("+-----+");
        for (int x = 0; x < SIZE; x++) stringBuilder.append("-----+");
        return stringBuilder.toString();
    }

    private static char[][] createMap() {
        //создание матрицы
        char[][] map = new char[SIZE][SIZE];
        for (char[] row: map) Arrays.fill(row, DOT_EMPTY);
        return map;
    }

    private static void putInXY(int[] xy, int x, int y) {
        //просто "ускоряем" размещение двух координат в элементе
        xy[X] = x;
        xy[Y] = y;
    }

    private static void clearXY(int[] xy) {
        //добавим проверку на null, потому что для обычной проверки победной последовательности
        //мы не создаем и не передаем точку "внутри"
        if (xy == null) return;
        //просто очистка
        xy[X] = -1;
        xy[Y] = -1;
    }

    private static void putInCoordinates(int[][] coordinates, int i, int x, int y) {
        //просто "ускоряем" размещение двух координат в массиве на нужном месте
        putInXY(coordinates[i], x, y);
    }

    private static boolean isEmptyXY(int[] xy) {
        //достаточно проверить X или Y на -1
        return xy[Y] == -1;
    }

    private static int findEmptyInCoordinates(int[][] coordinates) {
        //ищем первую ячейку пустую
        for (int i = 0; i < coordinates.length; i++) {
            if (isEmptyXY(coordinates[i]))
                return i;
        }
        //-1 - признак, что больше нет места
        return -1;
    }

    private static boolean putInCoordinatesWithCheck(char[][] map, int[][] coordinates,
                                                     int x, int y, char charWin, int dotsForWin,
                                                     int typeOfCoordinates, int[] xyInside) {
        //идея такая - иначально массив координат заполнен {-1, -1}
        //мы можем проверять, где еще свободно и автоматически
        //находить место для записи текущей координаты
        //если при этомы смотреть, какой символ получается в этих координатах
        //то можно вернуть что-то заранее, поняв, что больше не нужно
        //тогда последовательность символов будет и не нужна
        //а если размер матрицы 19х19, а ищем строку из 5 символов?
        //нужно вообще пропускать НЕ ТЕ символы...
        //так так, есть какая-то мысль...
        //возвращать будем ИСТИНУ, если накопилось нужное число символов
        //добавлен dotsForWin, потому что не всегда ищем победную последовательность, но и для блокировки тоже
        //для чего ищем будем смотреть по dotsForWin, если равна DOTS_TO_WIN, то это для победы
        //иначе - для блокировки и в этом случае мы будем обязательно проверять, свободна слева
        //или справа координата
        //для блокировки или победы ИИ может возникнуть ситуация, когда внутри почти победной
        //последовательности есть один пустой символ
        //куда можно было бы поставить свой символ либо для блокировки чужой последовательности
        //либо для своей победы. это всегда будет режим блокировки, т.е. когда ищем меньшую последовательность
        //чем нужную для победы
        //если такой найдется, мы его вернем в отдельном параметре xyInside, изначально там {-1, -1}

        int place = findEmptyInCoordinates(coordinates);
        //может вернуться -1, если уже нет места
        //такое вряд ли будет, но обработаем этот вариант
        if (place == -1) return false;

        //символ по пришедшим координатам
        char c = charFromMap(map, x, y);

        //обычный режим или это блокировка/попытка ИИ победить
        boolean blockMode = dotsForWin < DOTS_TO_WIN;
        //но искать внутри мы будем только одну ячейку
        //поэтому включаем его только, когда DOTS_TO_WIN - 1
        boolean insideMode = dotsForWin == DOTS_TO_WIN - 1;

        if (c == charWin) {
            //"пришел" нужный символ
            //мы помещаем в массив только нужные
            //поэтому смело его помещаем на место
            putInCoordinates(coordinates, place, x, y);
            //если накопилось нужное число, то больше и не копим
            //изначально тут было DOTS_TO_WIN, но мы используем этот алгоритм для поиска
            //последовательности любого числа символов для последующей блокировки
            if (place + 1 == dotsForWin) {
                //вот тут, если мы нашли нужное число символов
                //для именно блокировки нужно проверить точку слева и справа
                //если есть свободная, тогда нашли, иначе ищем дальше
                //но только, если эта точка не "внутри"
                if (blockMode && isEmptyXY(xyInside)) {
                    //именно это режим блокировки слева или справа
                    //нам нужно, чтобы слева или справа было пусто
                    int[] xy = leftDot(map, coordinates, typeOfCoordinates);
                    //если есть слева пустое место, это нам подходит
                    if (!isEmptyXY(xy)) return true;
                    //иначе смотрим справа
                    xy = rightDot(map, coordinates, typeOfCoordinates);
                    //если есть справа пустое место, это нам подходит
                    if (!isEmptyXY(xy)) return true;
                    //такая последовательность нам не подходит
                    //чистим ее и ищем дальше
                    clearCoordinates(coordinates, xyInside);
                    return false;
                }
                //а если внутри, то не чистим ее, чтобы вернулась
                //именно в режиме внутреннй блокировки
                if (insideMode && !isEmptyXY(xyInside))
                    return true;

                //если найдена последовательность, то нужно очистить место "внутри"
                clearXY(xyInside);

                return true;
            }

            //еще возможен вариант, когда не набрано нужное число символов, остался один свободный, но он внутри
            //только для внутренней блокировки
            //if (insideMode && place + 1 == dotsForWin - 1 && !isEmptyXY(xyInside)) {
            //но почему я тут беру на точку меньше, мы же должны набрать именно Н
            //но внутри одна пустая!!!
            if (insideMode && place + 1 == dotsForWin && !isEmptyXY(xyInside)) {
                //это тоже нужно считать подходящей последовательностью именно в режиме блокировки
                //попробуем вернуть, что найдено
                return true;
            }

            //если какое-то количество накопили, но недостаточно
            //мы уже не узнаем, будут еще символы или нет,
            //значит, не сможем очистить массив здесь, сделаем это там, где вызывается этот метод
        } else {
            //"пришел" ненужный символ
            //нужно не только НЕ помещать его в массив
            //но и стереть то, что уже там накопилось

            //но может быть режим внутренней блокировки, именно для него возможно исключение
            //не просто блокировки, а именно внутреней блокировки
            if (insideMode && c == DOT_EMPTY && isEmptyXY(xyInside) && place > 0) {
                //внутри искомой последовательности может быть один пустой символ
                //но только один, и до него должен был прийти хотя бы один "правильный"
                //запишем его в эту переменную и вернем false, чтобы продолжало копить
                putInXY(xyInside, x, y);
                return false;
            }

            clearCoordinates(coordinates, xyInside);
        }
        return false;
    }

    private static int checkSome(char[][] map, int[][] coordinates, char charWin,
                                 int dotsForWin, int[] xyInside) {
        //нужный для победы символ в charWin
        //массив координат всегда очищен (заполнен {-1, -1})
        //dotsForWin - сколько искать символов, для победы будет "победное", я для блокировки - другое
        //изначально возвращала булево - нет или есть такая, но нам нужно знать, ряд нашелся или что другое
        //0 - ничего, 1 - ряд/строка, 2 - колонка, 3 - главная диагональ, 4 - побочная диагональ
        //xyInside - чтобы в режиме блокировки или проверки победного хода ИИ вернуть координаты места внутри

        //все строки
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                //последовательно помещаем координаты в массив, там копятся только нужные
                //координаты, если накопилось нужно количество - выиграли
                if (putInCoordinatesWithCheck(map, coordinates, x, y, charWin, dotsForWin, ROW, xyInside))
                    return ROW;
            }
            //на всякий случай чистим массив победных координат для повторного использования
            clearCoordinates(coordinates, xyInside);
        }

        //все колонки
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (putInCoordinatesWithCheck(map, coordinates, x, y, charWin, dotsForWin, COLUMN, xyInside))
                    return COLUMN;
            }
            clearCoordinates(coordinates, xyInside);
        }

        //максимальный сдвиг для дополнительных диагоналей
        int maxShift = SIZE - DOTS_TO_WIN;

        for (int dy = -maxShift; dy <= maxShift; dy++) {
            //главная диагональ
            for (int y = 0; y < SIZE; y++) {
                int x = y - dy;
                //может получиться "вне" нашей матрицы, потому что через dy мы
                //ее сдвигаем вверх и вниз виртуально, поэтому добавляем проверку
                if (x < 0 || x >= SIZE) continue;
                if (putInCoordinatesWithCheck(map, coordinates, x, y, charWin, dotsForWin, MAIN_DIAG, xyInside))
                    return MAIN_DIAG;
            }
            clearCoordinates(coordinates, xyInside);
            //побочная диагональ
            for (int y = 0; y < SIZE; y++) {
                int x = SIZE - 1 - y - dy;
                if (x < 0 || x >= SIZE) continue;
                if (putInCoordinatesWithCheck(map, coordinates, x, y, charWin, dotsForWin, SIDE_DIAG, xyInside))
                    return SIDE_DIAG;
            }
            clearCoordinates(coordinates, xyInside);
        }

        //если ничего не нашлось...
        return NONE;
    }

    private static boolean mapIsNotFull(char[][] map) {
        //если найдется хотя бы одна пустая ячейка, значит, матрица еще не заполнена
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                if (charFromMap(map, x, y) == DOT_EMPTY)
                    return true;
            }
        }
        //иначе - заполнена полностью
        return false;
    }

    private static boolean humanTurn(char[][] map, Scanner scanner, char charWin) {
        //ход человека
        int x, y;

        do {

            //теперь (рандомный старт) может и у человека быть такой случай, что все заполнено
            if (!mapIsNotFull(map)) return true;

            System.out.printf("Введите X Y (две координаты от 1 до %d через пробел, 0 для выхода): ", SIZE);
            x = scanner.nextInt() - 1;

            //если хотят выйти, вернем ЛОЖЬ
            if (x == -1) return false;

            y = scanner.nextInt() - 1;

        } while (isNotCellValid(map, x, y));

        //ставим точку
        putToMap(map, charWin, x, y);
        System.out.printf("Ваш \"ход\" сделан в ячейку {%d, %d}\n", x + 1, y + 1);

        //если ход сделан, вернем ИСТИНУ
        return true;
    }

    private static void aiTurn(char[][] map, char charWin) {
        //ход ИИ
        //передаем сюда символ победы, потому что ИИ может играть сам с собой
        String otherAI = (charWin == DOT_X && AI_VS_AI) ? " (который за Вас)" : "";

        //получается, что ИИ все время только блокирует, а если у него есть
        //последовательность, где достаточно поставить один символ и будет победа
        //то это игнорируется
        //попробуем исправить, сначала проверяем DOTS_TO_WIN - 1 последовательность с местом
        //слеа или справа и закрываем ее
        //она же проверяет случай, когда место есть "внутри"
        int[] xy = aiTryWin(map, charWin);
        if (!isEmptyXY(xy)) {
            System.out.printf("ИИ%s побеждает, ставя %c в ячейку {%d, %d}\n", otherAI, charWin, xy[X] + 1, xy[Y] + 1);
            return;
        }

        //если наша настройка блокировки не = 0, то вместо случайных координат
        //мы сначала попробуем подобрать свои
        //передаем тот символ победы, которые ему нужно поставить
        //а блокировать он будет "другой"
        //там же он сам его и проставит
        //она же проверяет случай, когда место есть "внутри"
        xy = aiBlock(map, charWin, false);
        if (!isEmptyXY(xy)) {
            System.out.printf("ИИ%s ставит блок в ячейку {%d, %d}\n", otherAI, xy[X] + 1, xy[Y] + 1);
            return;
        }

        //еще один нерандомный вариант, когда нет явной победы за один ход
        //и нечего срочно блокировать
        //нужно в своих последовательностях, начиная с "победные минус 2"
        //пробовать поставить рядом или внутрь, а не делать рандомный ход
        xy = aiNoRandom(map, charWin);
        if (!isEmptyXY(xy)) {
            System.out.printf("ИИ%s осознанно \"ходит\" в ячейку {%d, %d}\n", otherAI, xy[X] + 1, xy[Y] + 1);
            return;
        }

        int x, y;
        do {

            //обнаружилось, что мы тут зависаем, если больше нет свободных клеток
            //например, матрица 3х3, девятый ход сделал человек - ничья
            //больше нет ячеек, а у нас выше алгоритм вызывает ход ИИ
            //можно было там сделать прерывание цикла еще в одном месте после ходя человека
            //но у нас может ИИ играть за человека, поэтому сделаем здесь
            if (!mapIsNotFull(map)) return;

            x = new Random().nextInt(SIZE);
            y = new Random().nextInt(SIZE);

        } while (isNotCellValid(map, x, y));

        //ставим точку
        putToMap(map, charWin, x, y);
        System.out.printf("ИИ%s \"ходит\" рандомно в ячейку {%d, %d}\n", otherAI, x + 1, y + 1);
    }

    private static int[] leftDot(char[][] map, int[][] coordinates, int typeOfCoordinates) {
        //общая функция по определению точки слева для найденной последовательности
        //в ней же мы проверим на пустоту, если занята, вернем {-1, -1}
        int[] xy = createXY();
        //начальный элемент массива
        int[] xyIn = coordinates[0];

        int x = xyIn[X];
        int y = xyIn[Y];
        //для строки нам нужна точка слева
        if (typeOfCoordinates == ROW)
            x--;
        //для колонки нужна точка выше
        if (typeOfCoordinates == COLUMN)
            y--;
        //для главной диагонали нужна точка выше/левее
        if (typeOfCoordinates == MAIN_DIAG) {
            x--;
            y--;
        }
        //для побочной диагонали нужна точка ниже/левее
        //было бы так, но у нас последовательность фомируется сверху вниз
        //поэтому левая имеет смысл "перед первой"
        //и мы будем брать выше/правее
        if (typeOfCoordinates == SIDE_DIAG) {
            x++;
            y--;
        }
        //если точка прошла все проверки и пустая, берем ее
        if (!isNotCellValid(map, x, y))
            putInXY(xy, x, y);
        //иначе вернем пустую {-1, -1}
        return xy;
    }

    private static int[] rightDot(char[][] map, int[][] coordinates, int typeOfCoordinates) {
        //общая функция по определению точки справа для найденной последовательности
        //в ней же мы проверим на пустоту, если занята, вернем {-1, -1}
        int[] xy = createXY();

        //найдем последний заполненный элемент массива, сначала - его размер
        int sizeBlock = findEmptyInCoordinates(coordinates);
        if (sizeBlock == -1) sizeBlock = coordinates.length;
        int[] xyIn = coordinates[sizeBlock - 1];

        int x = xyIn[X];
        int y = xyIn[Y];
        //для строки нам нужна точка справа
        if (typeOfCoordinates == ROW)
            x++;
        //для колонки нужна точка ниже
        if (typeOfCoordinates == COLUMN)
            y++;
        //для главной диагонали нужна точка ниже/правее
        if (typeOfCoordinates == MAIN_DIAG) {
            x++;
            y++;
        }
        //для побочной диагонали нужна точка выше/правее
        //было бы так, но у нас последовательность фомируется сверху вниз
        //поэтому правая имеет смысл "после последней"
        //и мы будем брать ниже/левее
        if (typeOfCoordinates == SIDE_DIAG) {
            x--;
            y++;
        }
        //если точка прошла все проверки и пустая, берем ее
        if (!isNotCellValid(map, x, y))
            putInXY(xy, x, y);
        //иначе вернем пустую {-1, -1}
        return xy;
    }

    private static int[] createXY() {
        int[] xy = new int[2];
        clearXY(xy);
        return xy;
    }

    private static int[] putInMapInsideOrLeftOrRight(char[][] map, char charWin, int[][] coordinates,
                                                     int[] xyInside, int typeOfCoordinates) {
        //прежде всего проверим, может есть точка/место внутри последовательности
        //она пришла в оттельной переменной
        if (!isEmptyXY(xyInside)) {
            putToMap(map, charWin, xyInside);
            return xyInside;
        }
        //у нас есть функция получения точки слева, причем она сразу проверяется на пустоту
        int[] xy = leftDot(map, coordinates, typeOfCoordinates);
        //если подходит, сразу в ней и ставим
        if (!isEmptyXY(xy)) {
            putToMap(map, charWin, xy);
        } else {
            //иначе берем ту, что справа
            xy = rightDot(map, coordinates, typeOfCoordinates);
            if (!isEmptyXY(xy)) {
                putToMap(map, charWin, xy);
            }
        }
        //всегда возвращаем какую-то точку, она будет пустой или заполненной
        return xy;
    }

    private static char charFromMap(char[][] map, int x, int y) {
        //один метод для получения символа из матрицы
        return map[y][x];
    }

    private static void putToMap(char[][] map, char c, int x, int y) {
        //один метод для установки значения в ячейке матрицы
        map[y][x] = c;
    }

    private static void putToMap(char[][] map, char c, int[] xy) {
        //перегруженный метод, согда сразу {X, Y} приходит
        putToMap(map, c, xy[X], xy[Y]);
    }

    private static int[] aiNoRandom(char[][] map, char charWin) {
        //как мне кажется, осознанный выбор не чем не отличается от
        //блокировки, только искать нужно свои же символы и ставить свои
        //это простой алгоритм, сложнее - это когда уже начиная со второго
        //символа, АИ пытается правильно его поставить, прикидывая, хватит ли места "с той стороны"
        //для выигрышной последовательности
        //да и первый символ можно ставить осознанно, чтобы было место тоже для всех остальных
        //это все здесь не реализуется, потому что так можно придумывать до бесконечности :)
        return aiBlock(map, charWin, true);
    }

    private static int[] aiTryWin(char[][] map, char charWin) {
        int[][] coordinates = createCoordinates();
        //проверяем, нет ли такой последовательности, где до победы останется всего один символ
        //и его можно поставить слева или справа
        //для варианта, когда его можно поставить "внутри" мы передаем специальную переменную
        int[] xyInside = createXY();
        int hasWin = checkSome(map, coordinates, charWin, DOTS_TO_WIN - 1, xyInside);
        if (hasWin == NONE) return xyInside;
        //здесь вызывается повторяющийся блок кода, для блокировки он такой и для попытки ИИ победить
        return putInMapInsideOrLeftOrRight(map, charWin, coordinates, xyInside, hasWin);
    }

    private static int[] aiBlock(char[][] map, char charWin, boolean noRandom) {
        //будем искать блокировки, если до победы у противника остается 2 шага
        int minDotsToBlock = DOTS_TO_WIN - 2;
        //для матрицы 3х3 все равно ищем 2
        if (minDotsToBlock == 1) minDotsToBlock = 2;
        //если получился 0, вообще не ищем ничего, но нужно вернуть "пустую" точку
        if (minDotsToBlock == 0) return createXY();
        //блокировать нужно символы чужие, т.е. "наоборот"
        char charBlock = noRandom ? charWin : charWin == DOT_O ? DOT_X : DOT_O;
        //сначала нужно поискать последовательность подлиннее, а потом уже, если
        //такая не нашлась, то блокировать более короткую
        //для осознанного хода, а не блокировки (флаг noRandom)
        //нам не нужно проверять DOTS_TO_WIN - 1, потому что он уже провере в "победной" проверке aiTryWin
        int start = noRandom ? DOTS_TO_WIN - 2 : DOTS_TO_WIN - 1;

        for (int dotsToBlock = start; dotsToBlock >= minDotsToBlock; dotsToBlock--) {
            int[][] coordinates = createCoordinates();
            //если нет такой последовательности, то делаем рандомый шаг
            //для варианта, когда блок можно поставить "внутри" мы передаем специальную переменную
            int[] xyInside = createXY();
            int hasBlock = checkSome(map, coordinates, charBlock, dotsToBlock, xyInside);
            if (hasBlock == NONE) continue;
            //здесь вызывается повторяющийся блок кода, для блокировки он такой и для попытки ИИ победить
            int[] xy = putInMapInsideOrLeftOrRight(map, charWin, coordinates, xyInside, hasBlock);
            if (isEmptyXY(xy)) continue;
            return xy;
        }
        //вернем пусто
        return createXY();
    }

    private static boolean isNotCellValid(char[][] map, int x, int y) {
        //проверка на правильность координат и на пустоту внутри ячейки
        return !(x >= 0 && x < SIZE && y >= 0 && y < SIZE && charFromMap(map, x, y) == DOT_EMPTY);
    }

    private static void printMap(char[][] map, String horLine, int[][] coordinatesWin) {
        //вывод на печати в "рамочках", с координатами
        //координаты "победы" нужны, чтобы отметить "победные" символы
        System.out.println(horLine);

        //выводим строку с координатами x
        System.out.print("|     |");
        for (int x = 0; x < SIZE; x++) {
            System.out.printf(" x=%d%s|", x + 1, x > 8 ? "" : " ");
        }
        System.out.println();

        //выводим матрицу, но слева сначала координаты y
        for (int y = 0; y < SIZE; y++) {
            System.out.println(horLine);
            System.out.printf("| y=%d%s|", y + 1, y > 8 ? "" : " ");
            for (int x = 0; x < SIZE; x++) {
                //если это именно победивший набор коородинат
                boolean b = isInCoordinates(coordinatesWin, x, y);
                //то выводим маркеры, чтобы было понятно, по какому именно набору
                //программа определила победу
                System.out.printf("%c %c %c|", b ? '>' : ' ', charFromMap(map, x, y), b ? '<' : ' ');
            }
            System.out.println();
        }

        System.out.println(horLine);
    }

    private static void clearCoordinates(int[][] coordinates, int[] xyInside) {
        //заполняем последовательность координатами {-1, -1}
        //раз приходится чистить последовательность
        //то нужно очистить и "место внутри", оно уже не нужно
        clearXY(xyInside);

        //обработаем и случай, когда тут пусто совсем
        if (coordinates.length == 0)
            return;

        //нет смысла чистить/заполнять, если в первом же элементе -1
        if (isEmptyXY(coordinates[0]))
            return;

        for (int i = 0; i < coordinates.length; i++) {
            clearXY(coordinates[i]);
        }
    }

    private static boolean isInCoordinates(int[][] coordinates, int x, int y) {
        //проверка наличия конкретных координат в массиве победных координат
        //чтобы вывести в той ячейке специальные маркеры
        for (int[] ints : coordinates) {
            if (ints[X] == x && ints[Y] == y)
                return true;
        }
        return false;
    }

}
