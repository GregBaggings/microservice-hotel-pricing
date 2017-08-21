package app.pricing;

import app.handlers.ResponseBuilder;
import app.models.Hotel;
import com.fasterxml.jackson.annotation.JsonInclude;
import app.handlers.ErrorHandler;
import app.models.Price;
import app.models.PriceDAO;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by Gergely_Agnecz on 8/1/2017.
 */
@RestController
public class PriceController {

    private RestTemplate restTemplate = new RestTemplate();
    private ResponseBuilder builder = new ResponseBuilder();
    private ErrorHandler incorrectInputHandler = new ErrorHandler("Incorrect input. Please use only numbers!");
    private ErrorHandler missingParameterHandler = new ErrorHandler("Missing param: ID");

    @Autowired
    PriceDAO priceDAO;

    @RequestMapping("/v1/hotels/prices")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ResponseEntity<?> roomPrices() {
        List<Price> prices = priceDAO.findAll();

        return new ResponseEntity<>(prices, HttpStatus.OK);
    }

    @RequestMapping("/v1/hotels/price")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ResponseEntity<?> roomPricesForAHotelByID(@Validated @RequestParam(value = "id", required = true) int id) {

        ResponseEntity<Hotel> hotelResponse = restTemplate.exchange("http://localhost:2222/v1/hotel?id={id}", HttpMethod.GET, null, new ParameterizedTypeReference<Hotel>() {
        }, id);

        Hotel hotel = hotelResponse.getBody();
        List<Price> prices = priceDAO.findAllByhotelId(id);

        if (prices.isEmpty() || hotel == null) {
            return new ResponseEntity<>(new ErrorHandler("No data was found for id: " + id), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(builder.buildResponse(hotel, prices), HttpStatus.OK);
    }

    @ExceptionHandler(TypeMismatchException.class)
    @ResponseBody
    public ResponseEntity<?> wrongType(Exception exception, HttpServletRequest request) {
        return new ResponseEntity<>(incorrectInputHandler, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ResponseEntity<?> missingParam(Exception exception, HttpServletRequest request) {
        return new ResponseEntity<>(missingParameterHandler, HttpStatus.BAD_REQUEST);
    }
}
