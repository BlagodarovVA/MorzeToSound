Утилита с графическим интерфейсом для преобразования текста в набр звуков Морзе.  
Параметры:  
- Частота звука,  
- длительность звукового сигнала точки (в мс). Длительность сигнала тире устанавливается в 3 раза больше, чем у точки.  
- Текст для преобразования.  

<img width="350" height="220" alt="image" src="https://github.com/BlagodarovVA/MorzeToSound/blob/main/src/main/java/Morze/Screenshot_1.png" />

# JDK 25.0.1  
# 1. Скомпилировать  
javac -encoding UTF-8 main/java/Morze/*.java  
  
# 2. Создать manifest.txt (вручную, с пустой строкой в конце!)  
Main-Class: main.java.Morze.TonePlayerGUI  
  
# 3. Собрать JAR  
jar cfm MorzePlayer.jar manifest.txt main/java/Morze/*.class  
  
# 4. Запустить  
java -jar MorzePlayer.jar  
  
