package ftn.iis.dto;

public class VektorskiUpitDto {
    private String message;

    public VektorskiUpitDto(String message) {
        this.message = message;
    }

    public VektorskiUpitDto() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
