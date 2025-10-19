package com.hophman.learning.juniemvc.rest

import com.hophman.learning.juniemvc.exception.ExistingEntityException
import com.hophman.learning.juniemvc.exception.NotFoundBeerException
import com.hophman.learning.juniemvc.exception.NotFoundEntityException
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
internal class GlobalRestExceptionHandler {

    @ExceptionHandler(NotFoundBeerException::class)
    fun handleNotFound(ex: NotFoundBeerException, request: HttpServletRequest): ResponseEntity<ProblemDetail> {
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message ?: "Not Found")
        problemDetail.setProperty("path", request.requestURI)

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail)
    }

    @ExceptionHandler(NotFoundEntityException::class)
    fun handleNotFoundGeneric(ex: NotFoundEntityException, request: HttpServletRequest):
            ResponseEntity<ProblemDetail> {
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message ?: "Not Found")
        problemDetail.setProperty("path", request.requestURI)

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.title = "Validation failed"
        problemDetail.detail = ex.bindingResult.fieldErrors.joinToString { "${it.field}: ${it.defaultMessage}" }
        problemDetail.setProperty("path", request.requestURI)

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        ex: ConstraintViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.title = "Constraint violation"
        problemDetail.detail = ex.constraintViolations.joinToString { "${it.propertyPath}: ${it.message}" }
        problemDetail.setProperty("path", request.requestURI)

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.title = "Invalid request body"
        problemDetail.detail = ex.mostSpecificCause?.message ?: ex.message
        problemDetail.setProperty("path", request.requestURI)

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail)
    }

    @ExceptionHandler(ExistingEntityException::class)
    fun handleConflict(
        ex: ExistingEntityException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT)
        problemDetail.title = "Conflict"
        problemDetail.detail = ex.message
        problemDetail.setProperty("path", request.requestURI)

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail)
    }
}