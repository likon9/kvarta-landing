-- Заявки с лендинга «Кварта» (соответствует сущности Order)
CREATE TABLE orders (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    -- нормализованный белорусский номер: +375XXXXXXXXX
    phone       VARCHAR(13)   NOT NULL,
    interest    VARCHAR(20)   NOT NULL,
    heard_from  VARCHAR(20),
    -- адрес страницы входа вместе с рекламными метками (utm_*, yclid, fbclid)
    landing_url VARCHAR(2048),
    comment     VARCHAR(2000),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT orders_phone_format CHECK (phone ~ '^\+375[0-9]{9}$'),
    CONSTRAINT orders_interest_values CHECK (
        interest IN ('SUBSCRIPTION', 'ETHIOPIA', 'COLOMBIA', 'BRAZIL', 'DRIP', 'QUESTION')
    )
);

-- Быстрая проверка «была ли заявка с этим номером за последние 10 минут»
CREATE INDEX idx_orders_phone_created_at ON orders (phone, created_at DESC);
