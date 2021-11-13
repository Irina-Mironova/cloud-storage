public class FileMessage implements Message {
    public String fileName;
    public byte[] arrayInfoBytes;
    public int numberPackage; //номер пакета
    public int countPackage; //

    public FileMessage(String fileName, byte[] arrayInfoBytes, int numberPackage, int countPackage) {
        this.fileName = fileName;
        this.arrayInfoBytes = arrayInfoBytes;
        this.numberPackage = numberPackage;
        this.countPackage = countPackage;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getArrayInfoBytes() {
        return arrayInfoBytes;
    }

    public int getNumberPackage() {
        return numberPackage;
    }

    public int getCountPackage() {
        return countPackage;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.FILE_MESSAGE;
    }
}
