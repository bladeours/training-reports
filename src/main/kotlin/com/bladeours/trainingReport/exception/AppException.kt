package com.bladeours.trainingReport.exception

import org.springframework.http.HttpStatus

class AppException(message: String, val httpStatus: HttpStatus) : Exception(message)
