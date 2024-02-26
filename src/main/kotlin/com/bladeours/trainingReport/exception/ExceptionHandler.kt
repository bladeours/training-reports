package com.bladeours.trainingReport.exception

import com.bladeours.trainingReport.exception.model.ErrorMessage
import io.github.oshai.kotlinlogging.KLogger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandler(private val log: KLogger) {

    @ExceptionHandler
    fun handleAppException(ex: AppException): ResponseEntity<ErrorMessage> =
        ResponseEntity(ex.toMessage(), ex.httpStatus)

    fun AppException.toMessage(): ErrorMessage =
        ErrorMessage(this.message.toString(), this.httpStatus.value())
}
