package cn.syx.registry.server.mode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionResponse {

    private HttpStatus httpStatus;
    private String errMsg;
}
