@echo off
set LIBRE_OFFICE="C:\Program Files\LibreOffice\program\soffice.exe"
set JAVA_PIPELINE=PPTXToJavaPipeline.java

echo Compiling Java pipeline...
javac %JAVA_PIPELINE%

echo Converting PPTX to PNG...
java PPTXToJavaPipeline

echo Packaging results...
cd converted
javac SlideViewer.java

echo Done! Use these commands to run:
echo   cd converted
echo   java SlideViewer
pause