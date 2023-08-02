package com.yes4all.constants;

public class GlobalConstant {

    public static final String FILE_UPLOAD_FOLDER_PATH = "fileupload/";
    public static final String KEY_NAME_UPLOAD = "&@#$%&";

    public static final String KEY_SPLIT = "@#@";
    public static final String FILE_UPLOAD = "fileupload";
    public static final String PATH_RESOURCE = "target/classes/template";
    //status PI
    public static final Integer STATUS_PI_NEW = 0;
    public static final Integer STATUS_PI_Y4A_REVIEW = 1;
    public static final Integer STATUS_PI_SUPPLIER_REVIEW = 2;
    public static final Integer STATUS_PI_Y4A_ADJUST = 3;
    public static final Integer STATUS_PI_SUPPLIER_APPROVED = 4;
    public static final Integer STATUS_PI_CONFIRMED = 5;
    public static final Integer STATUS_PI_CREATED = 6;

    public static final Integer STATUS_PI_CREATED_PKL = 7;
    public static final Integer STATUS_PI_LOADED = 8;
    public static final Integer STATUS_PI_ON_BOARDING = 9;
    public static final Integer STATUS_PI_DONE = 10;

    public static final Integer STATUS_PI_CANCEL = 11;
    //status PO
    public static final Integer STATUS_PO_NEW = 0;
    public static final Integer STATUS_PO_SENT = 1;
    public static final Integer STATUS_PO_PROCESSING = 2;
    public static final Integer STATUS_PO_PI_RECEIVED = 3;

    public static final Integer STATUS_PO_PI_CONFIRMED = 7;
    public static final Integer STATUS_PO_PI_SUPPLIER_APPROVED = 6;

    public static final Integer STATUS_PO_PI_Y4A_ADJUST = 5;
    public static final Integer STATUS_PO_PI_REVIEWING = 4;
    //status po when action BOOKING
    public static final Integer STATUS_PO_BOOKING_CREATED = 8;
    public static final Integer STATUS_PO_PACKING_LIST_CREATED = 9;
    public static final Integer STATUS_PO_LOADED = 10;
    public static final Integer STATUS_PO_ON_BOARDING = 11;

    public static final Integer STATUS_PO_DONE = 12;

    public static final Integer STATUS_PO_CANCEL = 13;
    //STATUS PACKING LIST
    public static final Integer STATUS_PACKING_LIST_WH_NEW = 0;

    public static final Integer STATUS_PACKING_LIST_WH_SUBMIT = 1;
    public static final Integer STATUS_PACKING_LIST_WH_CONFIRMED= 2;
    public static final Integer STATUS_PACKING_LIST_WH_REJECTED= 3;
    public static final Integer STATUS_PACKING_LIST_WH_ADJUSTED = 4;
    //STATUS US BROKER
    public static final Integer STATUS_SHIPMENT_US_BROKER_UPLOADED= 0;
    public static final Integer STATUS_SHIPMENT_US_BROKER_CONFIRMED= 1;
    public static final Integer STATUS_SHIPMENT_US_BROKER_REJECTED = 2;
    //STATUS PO NON DI
    public static final Integer STATUS_PO_WH_SHIPMENT_CREATED = 8;
    public static final Integer STATUS_PO_WH_PI_CI_CREATED = 9;
    public static final Integer STATUS_PO_WH_PI_CI_ADJUSTING = 10;
    public static final Integer STATUS_PO_WH_PIPELINE = 11;
    public static final Integer STATUS_PO_WH_DOCKED_US = 12;
    public static final Integer STATUS_PO_WH_IMPORTING = 13;
    public static final Integer STATUS_PO_WH_IMPORTED = 14;
    //STATUS SHIPMENT
    public static final Integer STATUS_SHIPMENT_STARTUP = 1;
    public static final Integer STATUS_SHIPMENT_REVIEW_PL_CI = 3;
    public static final Integer STATUS_SHIPMENT_PIPELINE = 4;
    public static final Integer STATUS_SHIPMENT_DOCKED_US = 5;
    public static final Integer STATUS_SHIPMENT_CONFIRMED_BROKER = 6;
    public static final Integer STATUS_SHIPMENT_REVIEW_BROKER = 7;
    public static final Integer STATUS_SHIPMENT_IMPORTING= 8;
    public static final Integer STATUS_SHIPMENT_COMPLETED= 9;
    //STATUS CONTAINER
    public static final Integer STATUS_CONTAINER_STARTUP = 1;
    public static final Integer STATUS_CONTAINER_ADJUSTING = 2;
    public static final Integer STATUS_CONTAINER_PIPELINE = 3;
    public static final Integer STATUS_CONTAINER_IMPORTING = 4;
    public static final Integer STATUS_CONTAINER_COMPLETED = 5;

    // role user
    public static final String POMS_USER = "POMS-USER";
    public static final String POMS_BROKER = "POMS-BROKER";
    public static final String POMS_WAREHOUSE = "POMS-WAREHOUSE";
    public static final String POMS_LOCAL_BROKER = "POMS-LOCAL-BROKER";
    public static final String POMS_LOGISTIC = "POMS-LOGISTIC";

    //Status Booking
    public static final Integer STATUS_BOOKING_WAITING_PKL = -2;
    public static final Integer STATUS_BOOKING_REVIEW_CI_PKL = -3;
    public static final Integer STATUS_BOOKING_UPLOAD = -1;
    public static final Integer STATUS_BOOKING_CREATED = 0;
    public static final Integer STATUS_BOOKING_LOADED = 1;
    public static final Integer STATUS_BOOKING_ON_BOARDING = 2;
    public static final Integer STATUS_BOOKING_COMPLETED = 3;

    public static final Integer STATUS_BOOKING_CANCEL = 4;
    //status packing list
    public static final Integer STATUS_PKL_NEW = 0;
    public static final Integer STATUS_PKL_SUBMIT = 1;
    public static final Integer STATUS_PKL_CONFIRMED = 2;
    public static final Integer STATUS_PKL_SEND = 3;
    //status CI
    public static final Integer STATUS_CI_NEW = 0;
    public static final Integer STATUS_CI_SENT_BUYER = 1;

    public static final Integer STATUS_CI_REJECT = 2;

    public static final Integer STATUS_CI_CONFIRMED = 3;


    //status CI
    public static final Integer STATUS_CI_DETAIL_REJECT = 1;
    public static final Integer STATUS_CI_NO_REJECT = 0;

    //status CI
    public static final Integer STATUS_CI_DETAIL_NO_ADJUSTED = 0;

    public static final Integer STATUS_CI_DETAIL_ADJUSTED = 1;

    //Status Split PO
    public static final Integer STATUS_PO_SPLIT_NEW = 1;
    public static final Integer STATUS_PO_SPLIT_SPLIT = 2;
    public static final Integer STATUS_PO_SPLIT_DELETE = 3;

    //Status BOL
    public static final Integer STATUS_BOL_NEW = 1;
    public static final Integer STATUS_BOL_UPDATE = 2;

    public static final Integer STATUS_BOL_SUBMIT = 3;
    public static final Integer STATUS_BOL_REQUEST = 4;
    public static final Integer STATUS_BOL_CONFIRMED = 5;

    //Status shipment
    public static final Integer STATUS_SHIPMENT_NEW = 1;



    // INFO COMPANY
    public static final String SOLD_TO_COMPANY = "Yes4ALL - LLC";
    public static final String SOLD_TO_ADDRESS = "3172 Nasa st, unit B (dock 12-17) , Brea, CA, USA, 92821";
    public static final String SOLD_TO_TELEPHONE = "713 360 3028";
    public static final String SOLD_TO_FAX = "";


    //Status booking detail
    public static final String STATUS_BOOKING_DETAIL_REASON_DEFAULT = "Select reason";

    // link get ship window soms

    public static final String PO_SHIP_DATE = "1";

    public static final String PO_SHIP_WINDOW = "2";
    public static final String PO_ACTUAL_SHIP_DATE = "3";
    public static final String PO_ETD = "4";
    public static final String PO_ETA = "5";
    public static final String PO_ATD = "6";
    public static final String PO_ATA = "7";

    public static final String SM_ETD = "8";

    public static final String SM_FILED_CONT = "SM_CONT";

    public static final String CONTAINER_TYPE_20 = "20' DC";

    public static final String CONTAINER_TYPE_40 = "40' HC";

    public static final String CONTAINER_TYPE_45 = "45' HC";

    public static final String REJECT_CI = "REJECT_CI";
    public static final String SEND_CI = "SEND_CI";
    public static final String CONFIRM_CI = "CONFIRM_CI";
    public static final String CONFIRM_SOURCING_PI = "CONFIRM_SOURCING_PI";
    public static final String CONFIRM_PU_PI = "CONFIRM_PU_PI";

    public static final String SUBMIT_BOL = "SUBMIT_BOL";
    public static final String CONFIRM_BOL = "CONFIRM_BOL";
    public static final String REQUEST_BOL = "REQUEST_BOL";

    public static final String SEND_REQUEST_SHIPMENT = "SEND_REQUEST_SHIPMENT";

    public static final String LINK_DETAIL_BOOKING = "/booking/detail/";

    //status not permission
    public static final String ERRORS_PERMISSION = "User is not permission!";

    //NAME SHEET PKL
    public static final String CONTAINER_PALLET_SHEET = "Container-Pallet Qty";
    //USER UPDATED LATEST PROFORMA INVOICE
    public static final Integer USER_UPDATED_PU = 2;
    public static final Integer USER_UPDATED_SOURCING = 1;
    public static final Integer USER_UPDATED_SUPPLIER = 3;
    //USER UPDATED LATEST PROFORMA INVOICE
    public static final Integer STATUS_SOURCING_ADJUST = 1;
    public static final Integer STATUS_SOURCING_CONFIRMED = 2;
    //USER ACTION
    public static final Integer STEP_ACTION_BY_PU = 2;
    public static final Integer STEP_ACTION_BY_SOURCING = 1;
    //USER
    public static final Integer STATUS_PU_ADJUST = 0;
    public static final Integer STATUS_PU_CONFIRMED = 1;

    //USER
    public static final Integer USER_SOURCING = 1;
    public static final Integer USER_PU = 2;
    public static final Integer USER_SUPPLIER = 3;

    //DEFINE TYPE SEARCH
    public static final String SEARCH_TEXT = "TEXT";
    public static final String SEARCH_TEXT_VN = "TEXT_VN";
    public static final String SEARCH_TEXT_DATE_FROM_TO = "TEXT_DATE_FROM_TO";
    public static final String SEARCH_TEXT_NUMBER_FROM_TO = "TEXT_NUMBER_FROM_TO";


}
