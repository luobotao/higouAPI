package utils;

import java.io.File;

public class FileWrapper {
private File file;
    
    public FileWrapper(File file) {
        this.file = file;
    }
     
    //排序
    public int compareTo(Object obj) {
        assert obj instanceof FileWrapper;
        
        FileWrapper castObj = (FileWrapper)obj;
                
        if (this.file.getName().compareTo(castObj.getFile().getName()) > 0) {
            return 1;
        } else if (this.file.getName().compareTo(castObj.getFile().getName()) < 0) {
            return -1;
        } else {
            return 0;
        }
    }
    
    public File getFile() {
        return this.file;
    }
}
