DROP TABLE IF EXISTS TRANSFER;
DROP TABLE IF EXISTS ACCOUNT;

CREATE TABLE ACCOUNT (
  ID BIGINT IDENTITY NOT NULL,
  BALANCE DECIMAL(20, 2) NOT NULL
);

CREATE TABLE TRANSFER (
  ID BIGINT IDENTITY NOT NULL,
  FROM_ACCOUNT_ID BIGINT NOT NULL,
  TO_ACCOUNT_ID BIGINT NOT NULL,
  AMOUNT DECIMAL(20, 2) NOT NULL,

  CONSTRAINT fk_t_transfer_from_account_id FOREIGN KEY (FROM_ACCOUNT_ID) REFERENCES ACCOUNT(ID),
  CONSTRAINT fk_t_transfer_to_account_id FOREIGN KEY (TO_ACCOUNT_ID) REFERENCES ACCOUNT(ID)
);

INSERT INTO ACCOUNT(BALANCE) values (350);
INSERT INTO ACCOUNT(BALANCE) values (250);
INSERT INTO ACCOUNT(BALANCE) values (500);
INSERT INTO ACCOUNT(BALANCE) values (1000);

INSERT INTO TRANSFER(FROM_ACCOUNT_ID,TO_ACCOUNT_ID,AMOUNT) values (3,4,30);
INSERT INTO TRANSFER(FROM_ACCOUNT_ID,TO_ACCOUNT_ID,AMOUNT) values (1,4,20);