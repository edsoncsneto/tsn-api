package br.edu.ifpe.dnc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDTO {

    private String message;
    private String error;

    public static ApiResponseDTO success(String message) {
        ApiResponseDTO dto = new ApiResponseDTO();
        dto.setMessage(message);
        return dto;
    }

    public static ApiResponseDTO error(String error) {
        ApiResponseDTO dto = new ApiResponseDTO();
        dto.setError(error);
        return dto;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
