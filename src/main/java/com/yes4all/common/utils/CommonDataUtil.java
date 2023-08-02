package com.yes4all.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yes4all.common.enums.ParamSearchListing;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.model.ListingDTO;
import com.yes4all.domain.User;
import org.apache.commons.codec.binary.Base64;
import org.apache.tomcat.jni.Local;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.regex.Pattern;

import static com.yes4all.common.constants.Constant.*;
import static com.yes4all.constants.GlobalConstant.*;

public class CommonDataUtil {
    private static final Logger log = LoggerFactory.getLogger(CommonDataUtil.class);

    public static final String SPACE = " ";

    public static final String DEFAULT_SYSTEM_USERNAME = "";
    public static ModelMapper modelMapper;

    public static boolean isEmpty(String str) {
        return Objects.isNull(str) || str.trim().length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !(collection == null || collection.isEmpty());
    }

    public static boolean isNull(Object obj) {
        return Objects.isNull(obj);
    }

    public static boolean isNotNull(Object obj) {
        return !isNull(obj);
    }

    public static boolean getBooleanValue(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    public static String toEmpty(String str) {
        return isEmpty(str) ? "" : str;
    }

    public static Object toZero(Object number) {
        return isNull(number) ? 0 : number;
    }

    public static String moneyFormatter(BigDecimal amount) {
        DecimalFormat formatter = new DecimalFormat("#,##0.00 USD");
        return formatter.format(amount);
    }


    public static ModelMapper getModelMapper() {
        if (Objects.isNull(modelMapper)) {
            modelMapper = new ModelMapper();
            modelMapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
        }
        return modelMapper;
    }

    public static boolean isDateValid(String date) {
        try {
            DateFormat df = new SimpleDateFormat("dd-MMM-yy");
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean isDateValidWH(String date) {
        try {
            DateFormat df = new SimpleDateFormat("mm/dd/yyyy");
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isDateValidSplitPO(String date) {
        try {
            DateFormat df = new SimpleDateFormat("dd-MMM-yy");
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getUserFullName(User user) {
        if (isNull(user)) {
            return DEFAULT_SYSTEM_USERNAME;
        }

        String firstName = Optional.ofNullable(user.getFirstName()).orElse("");
        String lastName = Optional.ofNullable(user.getLastName()).orElse("");
        if (isEmpty(firstName) && isEmpty(lastName)) {
            return DEFAULT_SYSTEM_USERNAME;
        }
        return String.join(SPACE, firstName, lastName).trim();

    }


    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(comparingByValueReverse());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
    public static String convertObjectToStringJson(Object object) {
        try {
            String json = "";
            if (!Objects.isNull(object)) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                json = mapper.writeValueAsString(object);
            }
            return json;
        }catch (Exception e){
            e.printStackTrace();
            e.getMessage();
        }
        return "";
    }
    public static <K, V extends Comparable<? super V>> Comparator<Map.Entry<K, V>> comparingByValueReverse() {
        return (Comparator<Map.Entry<K, V>> & Serializable)
            (c1, c2) -> c2.getValue().compareTo(c1.getValue());
    }


    public static Map<String, String> searchFilter(ListingDTO listingDTO) {
        Map<String, String> filterParams = new HashMap<>();
        listingDTO.setSearchBy(CommonDataUtil.isNotEmpty(listingDTO.getSearchBy()) ? listingDTO.getSearchBy() : "");
        listingDTO.setSearchByValue(CommonDataUtil.isNotEmpty(listingDTO.getSearchByValue()) ? listingDTO.getSearchByValue() : "");
        final String[] searchBy = {listingDTO.getSearchBy()};
        final String[] searchByValue = {listingDTO.getSearchByValue().toUpperCase()};
        String fromValue = (String) listingDTO.getFromValue();
        String toValue = (String) listingDTO.getToValue();
        String supplier = listingDTO.getSupplier();
        String userId = listingDTO.getUserId();
        List<ParamSearchListing> values = Arrays.asList(ParamSearchListing.values());
        values.forEach(item -> {
            String value = searchByValue[0];
            if (!searchBy[0].equals(item.getName())) {
                value = "";
            }
            if (!searchBy[0].equals("status") && item.getName().equals("status")) {
                value = "-1";
            }
            //fields search have character vietnam
            if (item.getType().equals(SEARCH_TEXT_VN)) {
                value = removeAccent(value).toUpperCase();
            }
            //fields search format from to
            if (item.getType().equals(SEARCH_TEXT_DATE_FROM_TO)) {
                if (searchBy[0].equals(item.getName())) {
                    filterParams.put(searchBy[0] + "From", fromValue);
                    filterParams.put(searchBy[0] + "To", toValue);
                } else {
                    filterParams.put(item.getName() + "From", "");
                    filterParams.put(item.getName() + "To", "");
                }
            } else if (item.getType().equals(SEARCH_TEXT_NUMBER_FROM_TO)) {
                if (searchBy[0].equals(item.getName())) {
                    filterParams.put(searchBy[0] + "From", fromValue);
                    filterParams.put(searchBy[0] + "To", toValue);
                } else {
                    filterParams.put(item.getName() + "From", "0");
                    filterParams.put(item.getName() + "To", "0");
                }
            } else {
                filterParams.put(item.getName(), value);
            }
        });
        filterParams.put("supplier", supplier);
        filterParams.put("userId", userId);
        return filterParams;
    }

    public static String removeAccent(String s) {

        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace("Đ", "D");
    }

    public static Map<String, Object> getAttributes(String jwtToken) {
        String[] splitString = jwtToken.split("\\.");
        String base64EncodedBody = splitString[1];

        Base64 base64Url = new Base64(true);

        String body = new String(base64Url.decode(base64EncodedBody));

        Map<String, Object> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            // convert JSON string to Map
            map = mapper.readValue(body,
                new TypeReference<Map<String, Object>>() {
                });
        } catch (Exception e) {
            log.error("Can't convert jsonString to mapObject");
        }
        return map;

    }


    public static boolean isInteger(String s) {
        return isInteger(s, 10);
    }

    public static boolean isInteger(String s, int radix) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) {
                    return false;
                } else {
                    continue;
                }
            }
            if (Character.digit(s.charAt(i), radix) < 0) return false;
        }
        return true;
    }


    public static String getSubjectMail(String poNumber, String country, User user) {
        //  ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
//        LocalDate date = LocalDate.ofInstant(orderDate, zone);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd yyyy");
//        String strDate = date.format(formatter);
        String vendor = user.getVendor();
        return "[" + vendor + "] Direct Import Order - " + country + " Order - (" + poNumber + ")";
    }

    public static String getSubjectMailWH(String poNumber, User user, LocalDate etd) {
        String vendor = user.getVendor();
        Month month = Month.of(etd.getMonthValue());
        String monthFormat = month.getDisplayName(TextStyle.SHORT, Locale.US);
        int weekOfYear = getWeekOfYear();
        return "[" + vendor + "] ORDER WEEK " + weekOfYear + " - " + poNumber + " (ETD_" + etd.getDayOfMonth() + "-" + monthFormat + ")";
    }

    public static String getSubjectMailBOL(String invoice) {
        return "US Broker Documents for BOL: " + invoice;
    }


    public static Integer getWeekOfYear() {
        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
        ZonedDateTime now = ZonedDateTime.now(zone);
        int week = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        return week;
    }

    public static Map<String, String> getInfoFile(String filename) {
        Map<String, String> info = new HashMap<>();
        String createBy = "";
        String dateUpload = "";
        String tempStr = filename.substring(filename.indexOf(KEY_NAME_UPLOAD) + KEY_NAME_UPLOAD.length());
        createBy = filename.substring(0, filename.indexOf(KEY_NAME_UPLOAD));
        createBy = createBy.replace("%20", " ");
        dateUpload = tempStr.substring(0, tempStr.indexOf(KEY_NAME_UPLOAD));
        filename = tempStr.substring(tempStr.indexOf(KEY_NAME_UPLOAD) + KEY_NAME_UPLOAD.length());
        info.put(CREATED_BY_UPLOAD, createBy);
        info.put(DATE_UPLOAD, dateUpload);
        info.put(FILE_NAME, filename);
        return info;
    }

    public static String contentMail(String link, String invoiceNo, String user, String object, String status, String type) {
        String htmlBody = "";
        String titleLink = "";
        if (type.equals("NEW")) {
            htmlBody = contentBodyMailCreateNewPO(user, invoiceNo);
            titleLink = "View Purchase Order";
        } else if (GlobalConstant.PO_SHIP_WINDOW.equals(type)) {
            htmlBody = contentBodyMailUpdateShipWindow(invoiceNo);
            titleLink = "View Purchase Order for update ship date";
        } else if (GlobalConstant.PO_SHIP_DATE.equals(type)) {
            htmlBody = contentBodyMailUpdateShipDate(invoiceNo);
            titleLink = "View update";
        } else if (GlobalConstant.REJECT_CI.equals(type)) {
            htmlBody = contentBodyMailRejectCI(invoiceNo);
            titleLink = "View Commercial Invoice";
        } else if (GlobalConstant.SEND_CI.equals(type)) {
            htmlBody = contentBodyMailSendCI();
            titleLink = "View Commercial Invoice";
        } else if (GlobalConstant.CONFIRM_SOURCING_PI.equals(type)) {
            htmlBody = contentBodyMailSourcingConfirmPI(invoiceNo);
            titleLink = "View Proforma Invoice";
        } else if (GlobalConstant.CONFIRM_CI.equals(type)) {
            htmlBody = contentBodyMailConfirmCI(invoiceNo);
            titleLink = "View Commercial Invoice";
        } else if (GlobalConstant.CONFIRM_PU_PI.equals(type)) {
            htmlBody = contentBodyMailPUConfirmPI();
            titleLink = "View Proforma Invoice";
        } else if (GlobalConstant.SUBMIT_BOL.equals(type)) {
            htmlBody = contentBodyMailSubmitBOL(user, invoiceNo);
            titleLink = "View Documents";
        } else if (GlobalConstant.REQUEST_BOL.equals(type)) {
            htmlBody = contentBodyMailRequestBOL(invoiceNo);
            titleLink = "View Documents";
        } else if (GlobalConstant.CONFIRM_BOL.equals(type)) {
            htmlBody = contentBodyMailConfirmBOL(invoiceNo);
            titleLink = "View Documents";
        }  else if (SEND_REQUEST_SHIPMENT.equals(type)) {
            String[] arrayData=invoiceNo.split("@");
            htmlBody = contentSendRequestShipment(arrayData[0],arrayData[1]);
            titleLink = "View Shipment";
        }else {
            htmlBody = contentBodyMailPO(user, invoiceNo, status, object);
            if (type.equals("Cancelled")) {
                titleLink = "View Purchase Order";
            } else if (type.equals("Adjusted")) {
                titleLink = "View PI Adjustment";
            } else if (type.equals("CreatedPI") || type.equals("Confirmed") || type.equals("Replied")) {
                titleLink = "View Proforma Invoice";
            } else if (type.equals("CreatedCI")) {
                titleLink = "View Commercial Invoice and Packing List";
            }
        }
        return "<!DOCTYPE html\n" +
            "    PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
            "\n" +
            "<head>\n" +
            "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
            "    <title>Demystifying Email Design</title>\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n" +
            "</head>\n" +
            "<style>\n" +
            "    body,\n" +
            "    table,\n" +
            "    td,\n" +
            "    a {\n" +
            "        -webkit-text-size-adjust: 100%;\n" +
            "        -ms-text-size-adjust: 100%;\n" +
            "    }\n" +
            "\n" +
            "    table,\n" +
            "    td {\n" +
            "        mso-table-lspace: 0pt;\n" +
            "        mso-table-rspace: 0pt;\n" +
            "    }\n" +
            "\n" +
            "    img {\n" +
            "        -ms-interpolation-mode: bicubic;\n" +
            "    }\n" +
            "\n" +
            "    img {\n" +
            "        border: 0;\n" +
            "        height: auto;\n" +
            "        line-height: 100%;\n" +
            "        outline: none;\n" +
            "        text-decoration: none;\n" +
            "    }\n" +
            "\n" +
            "    table {\n" +
            "        border-collapse: collapse !important;\n" +
            "    }\n" +
            "\n" +
            "    body {\n" +
            "        height: 100% !important;\n" +
            "        margin: 0 !important;\n" +
            "        padding: 0 !important;\n" +
            "        width: 100% !important;\n" +
            "    }\n" +
            "\n" +
            "    a[x-apple-data-detectors] {\n" +
            "        color: inherit !important;\n" +
            "        text-decoration: none !important;\n" +
            "        font-size: inherit !important;\n" +
            "        font-family: inherit !important;\n" +
            "        font-weight: inherit !important;\n" +
            "        line-height: inherit !important;\n" +
            "    }\n" +
            "\n" +
            "    div[style*=\"margin: 16px 0;\"] {\n" +
            "        margin: 0 !important;\n" +
            "    }\n" +
            "</style>" +
            "  <body\n" +
            "    style=\"background-color: #364894; margin: 0 auto !important; padding: 0 !important; width=\"80%\"\">" +
            "    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"80%\" style=\"margin: 0 auto\">\n" +
            "        <td bgcolor=\"#364894\" align=\"center\" style=\"padding: 0px 10px 0px 10px;\">" +
            "          <table style=\" margin-top: 40px;margin-bottom: 40px  \" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"720\">\n" +
            " <tr  border=\"1\">\n" +
            "                        <td>\n" +
            "                            <div style=\"margin: 0 auto; text-align: center; background-color: #fff; border-radius: 8px; margin-bottom: 40px; padding: 10px;\">\n" +
            "                                <img style=\"width: 180px; height: 80px;\" src=\"https://cdn.shopify.com/s/files/1/0595/0082/2724/files/logo_yes4all_color-2.png\" alt=\"logo\">\n" +
            "                            </div>\n" +
            "                        </td>\n" +
            "                    </tr>" +
            "    <tr style=\"margin: 0 auto; text-align: center; background-color: #fff; border-radius: 8px;margin-top: 20px;\">" +
            "            <tr>\n" +
            "              <td bgcolor=\"#ffffff\" align=\"left\">\n" +
            "                <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"\n style=\"color:#364894; text-align:left; margin-left: 100px; margin-right: 100px\" >" +
            "                  <tr>\n" +
            "                    <th\n" +
            "                      style=\"\n" +
            "                        font-family: Helvetica, Arial, sans-serif;\n" +
            "                        font-size: 16px;\n" +
            "                        font-weight: 600;\n" +
            "                        line-height: 50px;\n" +
            "                        color:  #364894;\n" +
            "                        padding: 40px;\n" +
            "                        width: 55%;" +
            "                        text-align: center;\n" +
            "                      \"\n" +
            "                    ></th>\n" +
            "                  </tr>\n" +
            htmlBody +
            "\n" +
            "                  <tr>\n" +
            "                    <th\n" +
            "                      align=\"center\"\n" +
            "                      valign=\"top\"\n" +
            "                      style=\"\n" +
            "                        font-family: Helvetica, Arial, sans-serif;\n" +
            "                        font-size: 16px;\n" +
            "                        font-weight: 400;\n" +
            "                        line-height: 25px;\n" +
            "                      \"\n" +
            "                    >\n" +
            "                      <a\n" +
            "                        href=\"" + link + "\"\n" +
            "                        target=\"_blank\"\n" +
            "                        style=\"\n" +
            "                          text-decoration: unset;\n" +
            "                          background: #194b9b;\n" +
            "                          border-radius: 5px;\n" +
            "                          width: 320px;\n" +
            "                          display: inline-block;\n" +
            "                          text-align: center;\n" +
            "                          color: #fff;\n" +
            "                          padding: 12px 0;\n" +
            "                          margin: 40px 0;\n" +
            "                          margin-right: 170px ;\n" +
            "                        \"\n" +
            "                        >" + titleLink + "</a\n" +
            "                      >\n" +
            "                    </th>\n" +
            "                  </tr>\n" +
            "\n" +
            "                </table>\n" +
            "              </td>\n" +
            "            </tr>\n" +
            "        <tr style=\"margin: 0 auto; text-align: center; color: #fff;\">\n" +
            "                        <td style=\" padding-top: 30px;\">\n" +
            "                            <b>Yes4All Trading Services Company Limited</b> <br /> 127 Hong Ha St., Ward 9, Phu Nhuan Dist., HCMC <br />\n" +
            "                            3172 Nasa Street, Unit B, Brea,\n" +
            "                            CA 92821 <br /> Website: <a style=\"color: #fff;\" href=\"https://yes4all.com/\" target=\"_blank\">https://yes4all.com/</a>\n" +
            "                        </td>\n" +
            "                    </tr>\n" +
            "                    <tr>\n" +
            "                        <td bgcolor=\"#ffffff\" align=\"center\">\n" +
            "                            <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n" +
            "                            </table>\n" +
            "                        </td>\n" +
            "                    </tr>" +
            "          </table>\n" +
            "        </td>\n" +
            "      </tr>\n" +
            "    </table>\n" +
            "  </body>\n" +
            "</html>\n" +
            "\n";
    }

    public static String contentBodyMailCreateNewPO(String supplier, String purchaserOrderNo) {
        LocalDate localDate = LocalDate.now().plusDays(2);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd yyyy");
        String strDate = localDate.format(formatter);
        return "                  <tr>" +
            "                             <td" +
            "                                 align=\\\"left\\\"\\\n" +
            "                                valign=\\\"top\\\"\\\n" +
            "                                  style=\\\"\\n" +
            "                                   padding-left: 60px;\n" +
            "                                  padding-right: 60px;\n" +
            "                                  padding-bottom: 10px;\n" +
            "                                   padding-top: 16px;\n" +
            "                                   font-family: Helvetica, Arial, sans-serif;\n" +
            "                                   font-size: 18px;\n" +
            "                                  font-weight: 400;\n" +
            "                                    line-height: 30px;\n" +
            "                                  \n" +
            "                               > Dear  <strong>  " + supplier + " </strong>,<br /> \n" +
            "                                We would like to send you a new purchase order <strong> " + purchaserOrderNo + " </strong> \n" +
            "                                 <br />\n" +
            "                             <p>   Kindly check and send us PI <span style=\"color:red;\">  <strong> before " + strDate + "  </strong> </span>.<p/> \n" +
            "                             </td> \n" +
            "                            </tr> ";
    }


    public static String contentBodyMailPO(String supplier, String purchaserOrderNo, String status, String object) {
        return "                  <tr>" +
            "                             <td" +
            "                                 text-align=\\\"center\\\"\\\n" +
            "                                valign=\\\"center\\\"\\\n" +
            "                                  style=\\\"\\n" +
            "                                   padding-left: 60px;\n" +
            "                                  padding-right: 60px;\n" +
            "                                  padding-bottom: 10px;\n" +
            "                                   padding-top: 16px;\n" +
            "                                   font-family: Helvetica, Arial, sans-serif;\n" +
            "                                   font-size: 18px;\n" +
            "                                  font-weight: 400;\n" +
            "                                    line-height: 30px;\n" +
            "                                    text-color: #364894;\n" +
            "                                  \n" +
            "                               > \n" +
            "                                " + object + " <strong> " + purchaserOrderNo + " </strong> has been " + status + " by " + (!supplier.equals("Yes4all") ? "supplier <strong>" + supplier + ".</strong>" : "Yes4All") + " \n" +
            "                                 <br />\n" +
            //  "                                Please check by clicking the button bellow. \n" +
            "                             </td> \n" +
            "                            </tr> ";
    }

    public static String contentBodyMailUpdateShipWindow(String purchaserOrderNo) {
        return "                  <tr>" +
            "                             <td" +
            "                                 align=\"center\"\n" +
            "                                valign=\"top\"\n" +
            "                                  style=\"\n" +
            "                                   padding-left: 30px;\n" +
            "                                  padding-right: 30px;\n" +
            "                                  padding-bottom: 10px;\n" +
            "                                   padding-top: 16px;\n" +
            "                                   font-family: Helvetica, Arial, sans-serif;\n" +
            "                                   font-size: 18px;\n" +
            "                                  font-weight: 400;\n" +
            "                                    line-height: 30px;\n\"" +
            "                                  \n" +
            "                               > \n" +
            "                                The Deadline submit booking for PO <strong><i> " + purchaserOrderNo + " </i></strong> has been updated" +
            "                                 <br />\n" +
            "                                Please update ship date to get the lasted information\n" +
            "                             </td> \n" +
            "                            </tr> ";
    }

    public static String contentBodyMailUpdateShipDate(String purchaserOrderNo) {
        return "                  <tr>" +
            "                             <td" +
            "                                 align=\"center\"\n" +
            "                                valign=\"top\"\n" +
            "                                  style=\"\n" +
            "                                   padding-left: 30px;\n" +
            "                                  padding-right: 30px;\n" +
            "                                  padding-bottom: 10px;\n" +
            "                                   padding-top: 16px;\n" +
            "                                   font-family: Helvetica, Arial, sans-serif;\n" +
            "                                   font-size: 18px;\n" +
            "                                  font-weight: 400;\n" +
            "                                    line-height: 30px;\n\"" +
            "                                  \n" +
            "                               > \n" +
            "                                The Ship date of PO <strong><i> " + purchaserOrderNo + " </i></strong> has been updated" +
            "                                 <br />\n" +
            "                             </td> \n" +
            "                            </tr> ";
    }

    public static String contentBodyMailConfirmCI(String purchaserOrderNo) {
        return "                  <tr>" +
            "                             <td" +
            "                                 align=\"center\"\n" +
            "                                valign=\"top\"\n" +
            "                                  style=\"\n" +
            "                                   padding-left: 30px;\n" +
            "                                  padding-right: 30px;\n" +
            "                                  padding-bottom: 10px;\n" +
            "                                   padding-top: 16px;\n" +
            "                                   font-family: Helvetica, Arial, sans-serif;\n" +
            "                                   font-size: 18px;\n" +
            "                                  font-weight: 400;\n" +
            "                                    line-height: 30px;\n\"" +
            "                                  \n" +
            "                               > \n" +
            "                              The Commercial Invoice and Packing List for PO <strong><i> " + purchaserOrderNo + " </i></strong> have been confirmed by Yes4All" +
            "                                 <br />\n" +
            "                             </td> \n" +
            "                            </tr> ";
    }

    public static String contentBodyMailSourcingConfirmPI(String invoiceNo) {
        return "                  <tr>" +
            "                             <td" +
            "                                 align=\"center\"\n" +
            "                                valign=\"top\"\n" +
            "                                  style=\"\n" +
            "                                   padding-left: 30px;\n" +
            "                                  padding-right: 30px;\n" +
            "                                  padding-bottom: 10px;\n" +
            "                                   padding-top: 16px;\n" +
            "                                   font-family: Helvetica, Arial, sans-serif;\n" +
            "                                   font-size: 18px;\n" +
            "                                  font-weight: 400;\n" +
            "                                    line-height: 30px;\n\"" +
            "                                  \n" +
            "                               > \n" +
            "                              The proforma invoice <strong><i> " + invoiceNo + " </i></strong> was checked by Sourcing" +
            "                                 <br />\n" +
            "                            Please click view Proforma Invoice to continue the process." +
            "                             </td> \n" +
            "                            </tr> ";
    }

    public static String contentBodyMailPUConfirmPI() {
        return "                  <tr>" +
            "                             <td" +
            "                                 align=\"center\"\n" +
            "                                valign=\"top\"\n" +
            "                                  style=\"\n" +
            "                                   padding-left: 30px;\n" +
            "                                  padding-right: 30px;\n" +
            "                                  padding-bottom: 10px;\n" +
            "                                   padding-top: 16px;\n" +
            "                                   font-family: Helvetica, Arial, sans-serif;\n" +
            "                                   font-size: 18px;\n" +
            "                                  font-weight: 400;\n" +
            "                                    line-height: 30px;\n\"" +
            "                                  \n" +
            "                               > \n" +
            "                             <strong>CBM,GW,NW,PCS/CTN,CTN,QTY </strong> need to be adjusted by Sourcing," +
            "                                       according to updated <strong>QTY</strong>." +
            "                                 <br />\n" +
            "                            Please click view Proforma Invoice to continue the process." +
            "                             </td> \n" +
            "                            </tr> ";
    }

    public static String contentBodyMailSubmitBOL(String nameBroker, String invoice) {
        return "                  <tr>" +
            "                             <td" +
            "                                 align=\"center\"\n" +
            "                                valign=\"top\"\n" +
            "                                  style=\"\n" +
            "                                   padding-left: 30px;\n" +
            "                                  padding-right: 30px;\n" +
            "                                  padding-bottom: 10px;\n" +
            "                                   padding-top: 16px;\n" +
            "                                   font-family: Helvetica, Arial, sans-serif;\n" +
            "                                   font-size: 18px;\n" +
            "                                  font-weight: 400;\n" +
            "                                    line-height: 30px;\n\"" +
            "                                  \n" +
            "                               > \n" +
            "                             US Broker <strong>" + nameBroker + "</strong> uploaded documents for bill of lading <strong>" + invoice + "</strong>." +
            "                                 <br />\n" +
            "                             </td> \n" +
            "                            </tr> ";
    }

    public static String contentBodyMailRequestBOL(String invoice) {
        return "                  <tr>" +
            "                             <td" +
            "                                 align=\"center\"\n" +
            "                                valign=\"top\"\n" +
            "                                  style=\"\n" +
            "                                   padding-left: 30px;\n" +
            "                                  padding-right: 30px;\n" +
            "                                  padding-bottom: 10px;\n" +
            "                                   padding-top: 16px;\n" +
            "                                   font-family: Helvetica, Arial, sans-serif;\n" +
            "                                   font-size: 18px;\n" +
            "                                  font-weight: 400;\n" +
            "                                    line-height: 30px;\n\"" +
            "                                  \n" +
            "                               > \n" +
            "                            Yes4all requested to re-upload US Broker documents for bill of lading <strong>" + invoice + "</strong>." +
            "                                 <br />\n" +
            "                             </td> \n" +
            "                            </tr> ";
    }

    public static String contentBodyMailConfirmBOL(String invoice) {
        return "                  <tr>" +
            "                             <td" +
            "                                 align=\"center\"\n" +
            "                                valign=\"top\"\n" +
            "                                  style=\"\n" +
            "                                   padding-left: 30px;\n" +
            "                                  padding-right: 30px;\n" +
            "                                  padding-bottom: 10px;\n" +
            "                                   padding-top: 16px;\n" +
            "                                   font-family: Helvetica, Arial, sans-serif;\n" +
            "                                   font-size: 18px;\n" +
            "                                  font-weight: 400;\n" +
            "                                    line-height: 30px;\n\"" +
            "                                  \n" +
            "                               > \n" +
            "                            Yes4all confirmed US Broker documents for bill of lading <strong>" + invoice + "</strong>." +
            "                                 <br />\n" +
            "                             </td> \n" +
            "                            </tr> ";
    }
    public static String contentSendRequestShipment(String invoice,String poNumber) {
        return "                  <tr>" +
            "                             <td>"+
            "                            Hello Logistics Team," +
            "                                 <br />\n" +
            "                                 <br />\n" +
            "                           A booking request for shipment <strong> "+invoice+" </strong> has been sent to you." +
            "                                 <br />\n" +
            "                          List purchaser orders: "+poNumber+"." +
            "                                 <br />\n" +
            "                          Please be aware and process." +
            "                             </td> \n" +
            "                            </tr> ";
    }
    public static String contentBodyMailRejectCI(String ci) {
        return "                  <tr>" +
            "                             <td" +
            "                                 align=\"center\"\n" +
            "                                valign=\"top\"\n" +
            "                                  style=\"\n" +
            "                                   padding-left: 30px;\n" +
            "                                  padding-right: 30px;\n" +
            "                                  padding-bottom: 10px;\n" +
            "                                   padding-top: 16px;\n" +
            "                                   font-family: Helvetica, Arial, sans-serif;\n" +
            "                                   font-size: 18px;\n" +
            "                                  font-weight: 400;\n" +
            "                                    line-height: 30px;\n\"" +
            "                                  \n" +
            "                               > \n" +
            "                                Unit Prices of some SKUs in the commercial invoice <strong><i> " + ci + " </i></strong> were rejected by Yes4All" +
            "                                 <br />\n" +
            "                             </td> \n" +
            "                            </tr> ";
    }

    public static String contentBodyMailSendCI() {
        return "                  <tr>" +
            "                             <td" +
            "                                 align=\"center\"\n" +
            "                                valign=\"top\"\n" +
            "                                  style=\"\n" +
            "                                   padding-left: 30px;\n" +
            "                                  padding-right: 30px;\n" +
            "                                  padding-bottom: 10px;\n" +
            "                                   padding-top: 16px;\n" +
            "                                   font-family: Helvetica, Arial, sans-serif;\n" +
            "                                   font-size: 18px;\n" +
            "                                  font-weight: 400;\n" +
            "                                    line-height: 30px;\n\"" +
            "                                  \n" +
            "                               > \n" +
            "                                Unit Prices of some SKUs have been adjusted by the supplier according to the requirements of Yes4All" +
            "                                 <br />\n" +
            "                             </td> \n" +
            "                            </tr> ";
    }

    public static String contentBodyMailAlertNewPO(Map<String, String> purchaseOrdersMap) {
        StringBuilder result = new StringBuilder();
        result.append("               <tr>" +
            "                             <td" +
            "                                 align=\"center\"\n" +
            "                                valign=\"top\"\n" +
            "                                  style=\"\n" +
            "                                   color: red;\n" +
            "                                   padding-left: 30px;\n" +
            "                                  padding-right: 30px;\n" +
            "                                  padding-bottom: 10px;\n" +
            "                                   padding-top: 16px;\n" +
            "                                   font-family: Helvetica, Arial, sans-serif;\n" +
            "                                   font-size: 18px;\n" +
            "                                  font-weight: 400;\n" +
            "                                    line-height: 30px;\n\"" +
            "                                  \n" +
            "                               > \n" +
            "                                There are some orders that have been created for more than 48 hours and\n" +
            "                                have not been sent to the supplier, please check and send them to the supplier\n" +
            "                                or delete them if not used" +
            "                                 <br />\n" +
            "                             </td> \n" +
            "                           </tr> ");

        for (Map.Entry<String, String> entry : purchaseOrdersMap.entrySet()) {
            result.append("                     <tr>" +
                "                             <td" +
                "                                 align=\"center\"\n" +
                "                                valign=\"top\"\n" +
                "                                  style=\"\n" +
                "                                   padding-left: 30px;\n" +
                "                                  padding-right: 30px;\n" +
                "                                  padding-bottom: 10px;\n" +
                "                                   padding-top: 16px;\n" +
                "                                   font-family: Helvetica, Arial, sans-serif;\n" +
                "                                   font-size: 18px;\n" +
                "                                  font-weight: 400;\n" +
                "                                    line-height: 30px;\n\"" +
                "                                  \n" +
                "                               > \n" +
                "                                <a\n" +
                "                                  href=\"" + entry.getKey() + "\"\n" +
                "                                  target=\"_blank\"\n" +
                "                                  style=\"\n" +
                "                                    text-decoration: unset;\n" +
                "                                    color: #194b9b;\n" +
                "                                  \"\n" +
                "                                  ><strong>" + entry.getValue() + "</strong></a\n" +
                "                                >\n" +

                "                                 <br />\n" +
                "                             </td> \n" +
                "                           </tr> ");
        }
        return result.toString();
    }

    public static String contentMailAlertNewPO(Map<String, String> purchaseOrdersMap) {
        String htmlBody = contentBodyMailAlertNewPO(purchaseOrdersMap);
        return contentMail(htmlBody);
    }

    public static String contentSendBooking(String booking, String proformaInvoice, String poAmazon, String supplier, Instant createdAt,
                                            String link, String titleLink) {
        createdAt = createdAt.plus(7, ChronoUnit.HOURS);
        String style = "                     align=\"left\"\n" +
            "                                valign=\"top\"\n" +
            "                                  style=\"\n" +
            "                                   padding-left: 30px;\n" +
            "                                  padding-right: 30px;\n" +
            "                                  padding-bottom: 10px;\n" +
            "                                   padding-top: 16px;\n" +
            "                                   font-family: Helvetica, Arial, sans-serif;\n" +
            "                                   font-size: 18px;\n" +
            "                                  font-weight: 400;\n" +
            "                                    line-height: 30px;\n\"" +
            "                                  \n";
        String styleHeader = "               align=\"center\"\n" +
            "                                valign=\"top\"\n" +
            "                                  style=\"\n" +
            "                                   padding-left: 30px;\n" +
            "                                  padding-right: 30px;\n" +
            "                                  padding-bottom: 10px;\n" +
            "                                   padding-top: 16px;\n" +
            "                                   font-family: Helvetica, Arial, sans-serif;\n" +
            "                                   font-size: 18px;\n" +
            "                                  font-weight: 400;\n" +
            "                                    line-height: 30px;\n\"" +
            "                                  \n";

        String htmlBody = "             <tr>" +
            "                             <td " + style +
            "                               > \n" +
            "                                <strong>This order will be shipped by this booking,</strong>\n" +
            "                                 <br />\n" +
            "                                <strong>please check and create Packing List & Commercial Invoice</strong>\n" +
            "                                 <br />\n" +
            "                             </td> \n" +
            "                           </tr> \n" +
            "             <tr>" +
            "                             <td " + style +
            "                               > \n" +
            "                                <strong>Booking:</strong> " + booking + " \n" +
            "                                 <br />\n" +
            "                             </td> \n" +
            "                           </tr> \n" +
            "                           <tr>" +
            "                             <td " + style +
            "                               > \n" +
            "                                <strong>Proforma Invoice:</strong> " + proformaInvoice + " \n" +
            "                                 <br />\n" +
            "                             </td> \n" +
            "                           </tr> " +
            "                           <tr>" +
            "                             <td " + style +
            "                               > \n" +
            "                                <strong>PO Amazon:</strong> " + poAmazon + " \n" +
            "                                 <br />\n" +
            "                             </td> \n" +
            "                           </tr> " +
            "                           <tr>" +
            "                             <td " + style +
            "                               > \n" +
            "                                <strong>Supplier:</strong> " + supplier + " \n" +
            "                                 <br />\n" +
            "                             </td> \n" +
            "                           </tr> " +
            "                           <tr>" +
            "                             <td " + style +
            "                               > \n" +
            "                                <strong>Created At:</strong> " + createdAt + " \n" +
            "                                 <br />\n" +
            "                             </td> \n" +
            "                           </tr> " +
            "                           \n" +
            "                           <tr>\n" +
            "                             <th " + styleHeader +
            "                             >\n" +
            "                               <a\n" +
            "                                 href=\"" + link + "\"\n" +
            "                                 target=\"_blank\"\n" +
            "                                 style=\"\n" +
            "                                   text-decoration: unset;\n" +
            "                                   background: #194b9b;\n" +
            "                                   border-radius: 5px;\n" +
            "                                   width: 320px;\n" +
            "                                   display: inline-block;\n" +
            "                                   text-align: center;\n" +
            "                                   color: #fff;\n" +
            "                                   padding: 12px 0;\n" +
            "                                   margin: 40px 0;\n" +
            "                                 \"\n" +
            "                                 >" + titleLink + "</a\n" +
            "                             </th>\n" +
            "                           </tr>\n" +
            "                           \n";
        return contentMail(htmlBody);
    }

    public static String contentMail(String htmlBody) {
        return "\n <!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
            "  <head>\n" +
            "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
            "    <title>Demystifying Email Design</title>\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n" +
            "  </head>\n" +
            "  <style>\n" +
            "    body,\n" +
            "    table,\n" +
            "    td,\n" +
            "    a {\n" +
            "      -webkit-text-size-adjust: 100%;\n" +
            "      -ms-text-size-adjust: 100%;\n" +
            "    }\n" +
            "\n" +
            "    table,\n" +
            "    td {\n" +
            "      mso-table-lspace: 0pt;\n" +
            "      mso-table-rspace: 0pt;\n" +
            "    }\n" +
            "\n" +
            "    img {\n" +
            "      -ms-interpolation-mode: bicubic;\n" +
            "    }\n" +
            "\n" +
            "    img {\n" +
            "      border: 0;\n" +
            "      height: auto;\n" +
            "      line-height: 100%;\n" +
            "      outline: none;\n" +
            "      text-decoration: none;\n" +
            "    }\n" +
            "\n" +
            "    table {\n" +
            "      border-collapse: collapse !important;\n" +
            "    }\n" +
            "\n" +
            "    body {\n" +
            "      height: 100% !important;\n" +
            "      margin: 0 !important;\n" +
            "      padding: 0 !important;\n" +
            "      width: 100% !important;\n" +
            "    }\n" +
            "\n" +
            "    a[x-apple-data-detectors] {\n" +
            "      color: inherit !important;\n" +
            "      text-decoration: none !important;\n" +
            "      font-size: inherit !important;\n" +
            "      font-family: inherit !important;\n" +
            "      font-weight: inherit !important;\n" +
            "      line-height: inherit !important;\n" +
            "    }\n" +
            "\n" +
            "    div[style*=\"margin: 16px 0;\"] {\n" +
            "      margin: 0 !important;\n" +
            "    }\n" +
            "  </style>\n" +
            "  <body\n" +
            "    style=\"background-color: #fff; margin: 0 !important; padding: 0 !important\"\n" +
            "  >\n" +
            "    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n" +
            "      <tr\n" +
            "        style=\"\n" +
            "          box-shadow: 0px 4px 4px rgba(0, 0, 0, 0.25);\n" +
            "          position: relative;\n" +
            "          z-index: 1;\n" +
            "          border-top: 8px solid #194b9b;\n" +
            "        \"\n" +
            "      >\n" +
            "        <td bgcolor=\"#FFF\" align=\"center\" style=\"padding: 0px 10px 0px 10px\">\n" +
            "          <div style=\"height: 90px; padding: 20px 0; box-sizing: border-box\">\n" +
            "            <img\n" +
            "              src=\"https://cdn.shopify.com/s/files/1/0595/0082/2724/files/logo_yes4all_color-2.png\"\n" +
            "              style=\"height: 60px\"\n" +
            "            />\n" +
            "          </div>\n" +
            "        </td>\n" +
            "      </tr>\n" +
            "      <tr>\n" +
            "        <td bgcolor=\"#fff\" align=\"center\" style=\"padding: 0px 10px 0px 10px\">\n" +
            "          <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"720\">\n" +
            "            <tr>\n" +
            "              <td bgcolor=\"#ffffff\" align=\"left\">\n" +
            "                <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n" +
            "                  <tr>\n" +
            "                    <th\n" +
            "                      style=\"\n" +
            "                        font-family: Helvetica, Arial, sans-serif;\n" +
            "                        font-size: 16px;\n" +
            "                        font-weight: 600;\n" +
            "                        line-height: 50px;\n" +
            "                        color: #fff;\n" +
            "                        padding: 40px;\n" +
            "                        width: 55%;\n" +
            "                      \"\n" +
            "                    ></th>\n" +
            "                  </tr>\n" +
            htmlBody +
            "\n" +
            "                  <tr>\n" +
            "                    <td\n" +
            "                      align=\"center\"\n" +
            "                      style=\"\n" +
            "                        padding-top: 26px;\n" +
            "                        padding-bottom: 15px;\n" +
            "                        font-family: Helvetica, Arial, sans-serif;\n" +
            "                        font-size: 14px;\n" +
            "                        font-weight: 400;\n" +
            "                        line-height: 25px;\n" +
            "                      \"\n" +
            "                    >\n" +
            "                      Thank You | Yes4All\n" +
            "                    </td>\n" +
            "                  </tr>\n" +
            "                  <tr>\n" +
            "                    <td\n" +
            "                      align=\"center\"\n" +
            "                      style=\"\n" +
            "                        padding: 15px 0;\n" +
            "                        font-family: Helvetica, Arial, sans-serif;\n" +
            "                        font-size: 16px;\n" +
            "                        font-weight: 400;\n" +
            "                        line-height: 25px;\n" +
            "                        border-top: 1px solid #ccc;\n" +
            "                      \"\n" +
            "                    ></td>\n" +
            "                  </tr>\n" +
            "                  <tr>\n" +
            "                    <td\n" +
            "                      align=\"center\"\n" +
            "                      style=\"\n" +
            "                        padding-bottom: 15px;\n" +
            "                        font-family: Helvetica, Arial, sans-serif;\n" +
            "                        font-size: 14px;\n" +
            "                        font-weight: 400;\n" +
            "                        line-height: 25px;\n" +
            "                      \"\n" +
            "                    >\n" +
            "                      (<span style=\"color: red\">*</span>) Please do not respond\n" +
            "                      to this email as responses are not monitored. <br />\n" +
            "                      If you have any questions, please\n" +
            "                      <a\n" +
            "                        href=\"mailto:nguyen.nguyenvt@yes4all.com\"\n" +
            "                        style=\"text-decoration: none\"\n" +
            "                        >email support</a\n" +
            "                      >.\n" +
            "                    </td>\n" +
            "                  </tr>\n" +
            "                </table>\n" +
            "              </td>\n" +
            "            </tr>\n" +
            "            <tr>\n" +
            "              <td bgcolor=\"#ffffff\" align=\"center\">\n" +
            "                <table\n" +
            "                  width=\"100%\"\n" +
            "                  border=\"0\"\n" +
            "                  cellspacing=\"0\"\n" +
            "                  cellpadding=\"0\"\n" +
            "                ></table>\n" +
            "              </td>\n" +
            "            </tr>\n" +
            "          </table>\n" +
            "        </td>\n" +
            "      </tr>\n" +
            "    </table>\n" +
            "  </body>\n" +
            "</html>\n" +
            "\n";
    }


}
