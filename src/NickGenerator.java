import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class NickGenerator {

    // Массивы для хранения количества найденных красивых слов разной длины
    private static final AtomicInteger count3 = new AtomicInteger(); // Количество красивых слов длиной 3, атомарные
    private static final AtomicInteger count4 = new AtomicInteger(); // Количество красивых слов длиной 4, атомарные
    private static final AtomicInteger count5 = new AtomicInteger(); // Количество красивых слов длиной 5, атомарные

    // Генератор случайных слов заданной длины
    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            /*
            например, random.nextInt(3) → может вернуть: 0, 1, 2
            letters.charAt(index) извлекает символ по указанному индексу
            text.append(char) добавляет указанный символ в конец текущего содержимого объекта text
             */
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        /*
        Метод toString() класса StringBuilder конвертирует содержимое объекта StringBuilder в стандартный
        объект типа String. Внутри этот метод создает новую строку на основе внутреннего буфера символов,
        накопленных в процессе работы с StringBuilder. Т.е. toString() преобразовывает объект типа
        StringBuilder обратно в обычную строку типа String.
         */
        return text.toString();
    }

    // Генерация набора слов (текстов)
    // Можем ожидать исключение InterruptedException, поскольку используем метод join()
    public static void main(String[] args) throws InterruptedException {

        // Генерируем 100 тыс. случайных слов длинной от 3 до 5 символов
        Random random = new Random();
        String[] texts = new String[100_000]; // Массив для хранения слов
        for (int i = 0; i < texts.length; i++) {
            texts[i] = generateText("abc", 3 + random.nextInt(3)); // длина случайная от 3 до 5 включительно
        }

        // Создадим три отдельных потока для параллельной проверки каждого условия
        // Потоки будут проверять каждое слово независимо друг от друга
        Thread palindromeThread = new Thread(() -> checkPalindromes(texts)); // для проверки палиндромов
        Thread sameLettersThread = new Thread(() -> checkSameLetters(texts)); // для проверки одинаковых букв
        Thread orderedThread = new Thread(() -> checkOrdered(texts)); // для проверки упорядоченности

        // Запускаем потоки
        palindromeThread.start();
        sameLettersThread.start();
        orderedThread.start();

        // Ждем завершение всех трех потоков
        // join() заставляет главный поток ждать завершения дочерних потоков перед продолжением своей работы
        palindromeThread.join();
        sameLettersThread.join();
        orderedThread.join();

        // Итоговая печать результатов
        System.out.println("Красивых слов с длиной 3: " + count3.get() + " шт");
        System.out.println("Красивых слов с длиной 4: " + count4.get() + " шт");
        System.out.println("Красивых слов с длиной 5: " + count5.get() + " шт");
    }

    // Метод проверки палиндромов (проверяет, совпадает ли первая половина строки со второй половиной в обратном порядке)
    private static boolean isPalindrome(String word) {
        if (word.isEmpty()) return false; // Если пустое слово, возвращаем false
        int left = 0; // Левый индекс
        int right = word.length() - 1; // Правый индекс
        while (left <= right && word.charAt(left) == word.charAt(right)) { // Пока индексы не пересеклись и совпадают символы
            left++; // Передвигаемся вправо
            right--; // Передвигаемся влево
        }
        /*
        В конце метода проверяется, завершился ли цикл нормально.
        Либо левая позиция прошла точку встречи с правой позицией (left > right), что означает успешную проверку всей строки.
        Либо позиции точно сравнялись (left == right), что тоже считается успешным случаем.
         */
        return left > right || left == right; // Вернуть true, если всё прошло успешно
    }

    // Метод проверки однотипности символов (сравниваем каждый символ с первым)
    private static boolean hasAllSameLetters(String word) {
        char firstChar = word.charAt(0); // Берём первый символ
        for (char ch : word.toCharArray()) { // Проходим по каждому символу
            if (ch != firstChar) return false; // Если хотя бы один символ отличается, вернём false
        }
        return true; // Иначе вернём true
    }

    // Метод проверки возрастающего порядка символов
    private static boolean isOrdered(String word) {
        char prev = 'a' - 1; // инициализируем символом меньше минимального возможного символа ('a')
        /*
        Дело в том, что в языке программирования Java символы представлены в виде целых чисел согласно
        таблице Unicode (или ASCII). Буква 'a' имеет определенный числовой код, и операция вычитания
        выполняется непосредственно с этим числом.
        Символ 'a' представлен значением 97 в таблице ASCII/Unicode. Поэтому выражение 'a' - 1 равносильно
        вычислению 97 - 1, что даёт число 96. Значению 96 соответствует специальный символ, расположенный
        непосредственно перед буквой 'a' в таблице символов.
         */

        for (char current : word.toCharArray()) { // Перебираем символы
            if (current < prev) return false; // Если текущий символ меньше предыдущего, возвращаем false
            prev = current; // Текущий становится предыдущим
        }
        return true; // Всё хорошо, возвращаем true
    }

    // Проверяем каждое слово на наличие свойства палиндрома
    private static void checkPalindromes(String[] words) {
        for (String word : words) { // Проход по всему списку слов
            if (isPalindrome(word)) { // Если слово — палиндром
                switch (word.length()) { // По длине определяем нужный счётчик
                    case 3 -> count3.incrementAndGet(); // Увеличиваем счётчик слов длиной 3
                    case 4 -> count4.incrementAndGet(); // Увеличиваем счётчик слов длиной 4
                    case 5 -> count5.incrementAndGet(); // Увеличиваем счётчик слов длиной 5
                }
            }
        }
    }

    // Проверяем каждое слово на однотипность символов
    private static void checkSameLetters(String[] words) {
        for (String word : words) {
            if (hasAllSameLetters(word)) { // Если все символы одинаковые
                switch (word.length()) { // По длине определяем нужный счётчик
                    case 3 -> count3.incrementAndGet();
                    case 4 -> count4.incrementAndGet();
                    case 5 -> count5.incrementAndGet();
                }
            }
        }
    }

    // Проверяем каждое слово на упорядоченность символов
    private static void checkOrdered(String[] words) {
        for (String word : words) {
            if (isOrdered(word)) { // Если символы упорядочены
                switch (word.length()) { // По длине определяем нужный счётчик
                    case 3 -> count3.incrementAndGet();
                    case 4 -> count4.incrementAndGet();
                    case 5 -> count5.incrementAndGet();
                }
            }
        }
    }
}