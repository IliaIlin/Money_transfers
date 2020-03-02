package moneytransfers;

import com.google.inject.Guice;
import moneytransfers.controller.MoneyTransferController;

public class MoneyTransferApplication {

    public static void main(String[] args) {
        Guice.createInjector(new MoneyTransferModule())
                .getInstance(MoneyTransferController.class)
                .run();
    }
}
