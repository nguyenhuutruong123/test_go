package com.yes4all.service;


import com.yes4all.domain.Booking;
import com.yes4all.domain.PurchaseOrders;
import com.yes4all.domain.model.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Service Interface for managing {@link PurchaseOrders}.
 */
public interface BookingService {
    BookingDTO createBooking(BookingDTO bookingDTO) ;
    List<BookingMainDTO> getAllBookingListBookingNo(SearchBookingDTO request) ;
    void updateStatusResource(Booking booking);
    BookingDTO completedBooking(Integer id) ;
    boolean sendPackingList(Integer id);
    BookingDTO sendBooking(SendBookingDTO request) ;

    boolean deleteBooking(Integer id);
    ResultUploadBookingDTO save(MultipartFile file,String userId);
    BookingPackingListDTO submitPackingList( BookingPackingListDTO bookingPackingListDTO,Integer id  ) ;
    BookingPackingListDTO confirmPackingList( BodyConfirmCIDTO request) ;

    BookingDetailsDTO getBookingDetailsDTO(BookingPageGetDetailDTO id);

    Integer updateBooking(BookingDetailsDTO request);

    ResultDTO cancelBooking(Integer id);

    BookingPackingListDTO getPackingListDetailsDTO(BodyGetDetailDTO request);

    BookingPackingListDTO getDetailsCreatedPackingList(ListIdDTO request );


    Page<BookingMainDTO> listingBookingWithCondition(Integer page, Integer limit, Map<String, String> filterParams);
    void export(String filename,List<Integer> id) throws IOException;

    void     exportPKLFromBOL(String filename,Integer id) throws IOException;

}
