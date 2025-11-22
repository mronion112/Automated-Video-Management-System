package org.example.Module.Google.Sheet;

public class RowSheet extends SheetObjectModun{
    int numberPosition;
    public RowSheet(int numberPosition, String name, String linkDriveFolder, String duration, String status) {
        super(name, linkDriveFolder, duration, status);
        this.numberPosition = numberPosition;
    }

    public int getNumberPosition() {
        return numberPosition;
    }

    public void setNumberPosition(int numberPosition) {
        this.numberPosition = numberPosition;
    }

    @Override
    public String toString() {
        return "RowSheet{" +
                "numberPosition=" + numberPosition +
                "name='" + getName() + '\'' +
                ", linkDriveFolder='" + getLinkDriveFolder() + '\'' +
                ", duration=" + getDuration() +
                ", status=" + getStatus() +
                "}\n";
    }
}
