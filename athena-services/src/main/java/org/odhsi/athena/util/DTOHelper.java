package org.odhsi.athena.util;

/**
 * Created by GMalikov on 28.07.2015.
 */
public class DTOHelper {

    private DTOHelper() {

    }

    public static long calculateTotalPages(long recordsTotal, long recordsPerPage) {
        long result = 0;
        if (recordsPerPage > 0) {
            result = recordsTotal / recordsPerPage;
            if (recordsTotal % recordsPerPage > 0) {
                result = result + 1;
            }
        }
        return result;
    }

    public static String checkSortOrder(String sortOrder) {
        if ("asc".equals(sortOrder) || "desc".equals(sortOrder)) {
            return sortOrder;
        } else {
            return "desc";
        }
    }
}
