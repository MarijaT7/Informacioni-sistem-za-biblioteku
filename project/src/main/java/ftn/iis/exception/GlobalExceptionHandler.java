package ftn.iis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //dodati user not found ??

    @ExceptionHandler(NonManagerCreatingSupplierException.class)
    public ResponseEntity<String> handleNonMenagerCreatingSupplierException(NonManagerCreatingSupplierException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(SupplierPibAlreadyExists.class)
    public ResponseEntity<String> handleSupplierPibAlreadyExists(SupplierPibAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(SupplierEmailAlreadyExists.class)
    public ResponseEntity<String> handleSupplierEmailAlreadyExists(SupplierEmailAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(SupplierPhoneAlreadyExists.class)
    public ResponseEntity<String> handleSupplierPhoneAlreadyExists(SupplierPhoneAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(SupplierNameAlreadyExists.class)
    public ResponseEntity<String> handleSupplierNameAlreadyExists(SupplierNameAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(NonManagerViewingSupplierException.class)
    public ResponseEntity<String> handleNonManagerViewingSupplierException(NonManagerViewingSupplierException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(NoSupplierFound.class)
    public ResponseEntity<String> handleNoSupplierFound(NoSupplierFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(NonManagerUpdatingSupplierException.class)
    public ResponseEntity<String> handleNonMenagerUpdatingSupplierException(NonManagerUpdatingSupplierException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(NonManagerDeletingSupplierException.class)
    public ResponseEntity<String> handleNonMenagerDeletingSupplierException(NonManagerDeletingSupplierException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(NonManagerCreatingContractException.class)
    public ResponseEntity<String> handleNonManagerCreatingContractException(NonManagerCreatingContractException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(SupplierNotActiveException.class)
    public ResponseEntity<String> handleSupplierNotActiveException(SupplierNotActiveException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(ActiveContractAlreadyExistsException.class)
    public ResponseEntity<String> handleActiveContractAlreadyExistsException(ActiveContractAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidContractDateException.class)
    public ResponseEntity<String> handleInvalidContractDateException(InvalidContractDateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

}
