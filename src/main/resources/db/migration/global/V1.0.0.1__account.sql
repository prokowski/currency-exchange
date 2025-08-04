CREATE SEQUENCE hibernate_sequence;

------------------------------------------------------------------------------

CREATE TABLE ACCOUNT
(
    ENTITY_ID          bigint                NOT NULL,
    ENTITY_VERSION     bigint                NOT NULL,
    ACCOUNT_ID         varchar(255)          NOT NULL,
    FIRST_NAME         varchar(50)           NOT NULL,
    LAST_NAME          varchar(50)           NOT NULL,
    CONSTRAINT ACCOUNT_PK PRIMARY KEY (ENTITY_ID),
    CONSTRAINT ACCOUNT_ID_UNIQUE UNIQUE (ENTITY_ID, ACCOUNT_ID)
);

------------------------------------------------------------------------------

CREATE TABLE ACCOUNT_WALLET
(
    ENTITY_ID           bigint               NOT NULL,
    ACCOUNT_ENTITY_ID   bigint               NOT NULL,
    CURRENCY_CODE       varchar(3)           NOT NULL,
    BALANCE             numeric(38,2)        NOT NULL,
    CONSTRAINT ACCOUNT_WALLET_PK PRIMARY KEY (ENTITY_ID),
    CONSTRAINT ACCOUNT_WALLET_UNIQUE UNIQUE (ENTITY_ID)
);

------------------------------------------------------------------------------

CREATE TABLE SUPPORTED_CURRENCY
(
    CURRENCY_CODE        varchar(3)           NOT NULL,
    CONSTRAINT SUPPORTED_CURRENCY_PK PRIMARY KEY (CURRENCY_CODE),
    CONSTRAINT SUPPORTED_CURRENCY_UNIQUE UNIQUE (CURRENCY_CODE)
);

------------------------------------------------------------------------------

INSERT INTO SUPPORTED_CURRENCY (CURRENCY_CODE) VALUES ('PLN');
INSERT INTO SUPPORTED_CURRENCY (CURRENCY_CODE) VALUES ('USD');