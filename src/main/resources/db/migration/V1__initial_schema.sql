create table users (
    id bigserial primary key,
    email varchar(255) not null unique,
    full_name varchar(120) not null,
    password_hash varchar(255) not null,
    created_at timestamptz not null
);

create table plans (
    id bigserial primary key,
    owner_id bigint not null references users(id) on delete cascade,
    name varchar(120) not null,
    price_cents integer not null,
    currency varchar(3) not null,
    billing_interval varchar(20) not null,
    trial_days integer not null,
    active boolean not null,
    created_at timestamptz not null
);

create table customers (
    id bigserial primary key,
    owner_id bigint not null references users(id) on delete cascade,
    email varchar(255) not null,
    name varchar(120) not null,
    company varchar(120),
    created_at timestamptz not null,
    unique(owner_id, email)
);

create table subscriptions (
    id bigserial primary key,
    owner_id bigint not null references users(id) on delete cascade,
    customer_id bigint not null references customers(id),
    plan_id bigint not null references plans(id),
    status varchar(30) not null,
    current_period_start date not null,
    current_period_end date not null,
    canceled_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table invoices (
    id bigserial primary key,
    owner_id bigint not null references users(id) on delete cascade,
    customer_id bigint not null references customers(id),
    subscription_id bigint not null references subscriptions(id),
    amount_cents integer not null,
    currency varchar(3) not null,
    status varchar(30) not null,
    due_date date not null,
    paid_at timestamptz,
    created_at timestamptz not null
);

create index idx_plans_owner_id on plans(owner_id);
create index idx_customers_owner_id on customers(owner_id);
create index idx_subscriptions_owner_id on subscriptions(owner_id);
create index idx_subscriptions_status on subscriptions(status);
create index idx_invoices_owner_id on invoices(owner_id);
create index idx_invoices_status on invoices(status);
