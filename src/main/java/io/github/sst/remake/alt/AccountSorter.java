package io.github.sst.remake.alt;
import io.github.sst.remake.Client;

import java.util.*;

public class AccountSorter {
    public static List<Account> sortByInputAltAccounts(AccountCompareType compareType, String address, String input) {
        List<Account> sortedAccounts = sort(compareType, address);
        input = input.toLowerCase();
        if (!input.isEmpty()) {
            List<Account> matchedAccounts = new ArrayList<>();
            Iterator<Account> accountIterator = sortedAccounts.iterator();

            while (accountIterator.hasNext()) {
                Account account = accountIterator.next();
                if (account.name.toLowerCase().startsWith(input)) {
                    matchedAccounts.add(account);
                    accountIterator.remove();
                }
            }

            Iterator<Account> accountIterator2 = sortedAccounts.iterator();

            while (accountIterator2.hasNext()) {
                Account account = accountIterator2.next();
                if (account.name.toLowerCase().contains(input)) {
                    matchedAccounts.add(account);
                    accountIterator2.remove();
                }
            }

            matchedAccounts.addAll(sortedAccounts);
            return matchedAccounts;
        } else {
            return sortedAccounts;
        }
    }

    public static List<Account> sort(AccountCompareType type, String address) {
        List<Account> sortedList = new ArrayList<>(Client.INSTANCE.accountManager.accounts);

        switch (type) {
            case ALPHABETICAL:
                sortedList.sort(Comparator.comparing(a -> a.name.toLowerCase()));
                break;

            case BANS:
                sortedList.sort((a1, a2) -> {
                    AccountBan ban1 = a1.getBanInfo(address);
                    AccountBan ban2 = a2.getBanInfo(address);

                    Date date1 = (ban1 != null) ? ban1.date : new Date();
                    Date date2 = (ban2 != null) ? ban2.date : new Date();

                    if (ban1 != null && ban2 != null) {
                        long diff1 = date1.getTime() - System.currentTimeMillis();
                        long diff2 = date2.getTime() - System.currentTimeMillis();

                        if (diff1 < 0 && diff2 < 0) {
                            return date2.compareTo(date1);
                        }
                    }

                    return date1.compareTo(date2);
                });
                break;

            case ADDED:
                sortedList.sort((a1, a2) -> Long.compare(a2.dateAdded, a1.dateAdded));
                break;

            case LAST_USED:
                sortedList.sort((a1, a2) -> Long.compare(a2.lastUsed, a1.lastUsed));
                break;

            case USE_COUNT:
                sortedList.sort(Comparator.comparingInt(a -> a.useCount));
                break;
        }

        return sortedList;
    }
}
