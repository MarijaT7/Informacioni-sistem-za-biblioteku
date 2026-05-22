package ftn.iis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //dodati user not found ??

    @ExceptionHandler(NonManagerCreatingSupplierException.class)
    public ResponseEntity<String> handleUserNotFound(NonManagerCreatingSupplierException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(SupplierPibAlreadyExists.class)
    public ResponseEntity<String> handleUserNotFound(SupplierPibAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(SupplierEmailAlreadyExists.class)
    public ResponseEntity<String> handleUserNotFound(SupplierEmailAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(SupplierPhoneAlreadyExists.class)
    public ResponseEntity<String> handleUserNotFound(SupplierPhoneAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(SupplierNameAlreadyExists.class)
    public ResponseEntity<String> handleUserNotFound(SupplierNameAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

}
