package main.java.Morze;

public class MorzeDictionary {

    public static StringBuilder stringToMorze(String str){

    StringBuilder sb = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            switch (str.charAt(i)){
                case ' ':
                    sb.append("   "); // три пробела между словами
                    break;

                // Русские буквы
                case 'А':
                    sb.append(".- ");
                    break;
                case 'Б':
                    sb.append("-... ");
                    break;
                case 'В':
                    sb.append(".-- ");
                    break;
                case 'Г':
                    sb.append("--. ");
                    break;
                case 'Д':
                    sb.append("-.. ");
                    break;
                case 'Е':
                case 'Ё': // Ё кодируется так же, как Е
                    sb.append(". ");
                    break;
                case 'Ж':
                    sb.append("...- ");
                    break;
                case 'З':
                    sb.append("--.. ");
                    break;
                case 'И':
                    sb.append(".. ");
                    break;
                case 'Й':
                    sb.append(".--- ");
                    break;
                case 'К':
                    sb.append("-.- ");
                    break;
                case 'Л':
                    sb.append(".-.. ");
                    break;
                case 'М':
                    sb.append("-- ");
                    break;
                case 'Н':
                    sb.append("-. ");
                    break;
                case 'О':
                    sb.append("--- ");
                    break;
                case 'П':
                    sb.append(".--. ");
                    break;
                case 'Р':
                    sb.append(".-. ");
                    break;
                case 'С':
                    sb.append("... ");
                    break;
                case 'Т':
                    sb.append("- ");
                    break;
                case 'У':
                    sb.append("..- ");
                    break;
                case 'Ф':
                    sb.append("..-. ");
                    break;
                case 'Х':
                    sb.append(".... ");
                    break;
                case 'Ц':
                    sb.append("-.-. ");
                    break;
                case 'Ч':
                    sb.append("---. ");
                    break;
                case 'Ш':
                    sb.append("---- ");
                    break;
                case 'Щ':
                    sb.append("--.- ");
                    break;
                case 'Ъ':
                    sb.append(".--.-. ");
                    break;
                case 'Ы':
                    sb.append("-.-- ");
                    break;
                case 'Ь':
                    sb.append("-..- ");
                    break;
                case 'Э':
                    sb.append("..-.. ");
                    break;
                case 'Ю':
                    sb.append("..-- ");
                    break;
                case 'Я':
                    sb.append(".-.- ");
                    break;

                // Цифры
                case '0':
                    sb.append("----- ");
                    break;
                case '1':
                    sb.append(".---- ");
                    break;
                case '2':
                    sb.append("..--- ");
                    break;
                case '3':
                    sb.append("...-- ");
                    break;
                case '4':
                    sb.append("....- ");
                    break;
                case '5':
                    sb.append("..... ");
                    break;
                case '6':
                    sb.append("-.... ");
                    break;
                case '7':
                    sb.append("--... ");
                    break;
                case '8':
                    sb.append("---.. ");
                    break;
                case '9':
                    sb.append("----. ");
                    break;

                // Знаки препинания
                case '.':
                    sb.append(".-.-.- ");
                    break;
                case ',':
                    sb.append("--..-- ");
                    break;
                case '?':
                    sb.append("..--.. ");
                    break;
                case '!':
                    sb.append("-.-.-- ");
                    break;
                case ':':
                    sb.append("---... ");
                    break;
                case ';':
                    sb.append("-.-.-. ");
                    break;
                case '-':
                    sb.append("-....- ");
                    break;
                case '"':
                    sb.append(".-..-. ");
                    break;
                case '(':
                case ')':
                    sb.append("-.--.- ");
                    break;
                case '\'':
                    sb.append(".----. ");
                    break;
                case '/':
                    sb.append("-..-. ");
                    break;
                case '_':
                    sb.append("..--.- ");
                    break;
                case '@':
                    sb.append(".--.-. ");
                    break;
                case '=':
                    sb.append("-...- ");
                    break;
                case '+':
                    sb.append(".-.-. ");
                    break;
                case '&':
                    sb.append(".-... ");
                    break;

                default: break;
            }
        }

        // Убираем завершающий пробел, если он есть
        if (!sb.isEmpty() && sb.charAt(sb.length() - 1) == ' ') {
            sb.setLength(sb.length() - 1);
        }

        return sb;
    }
}
