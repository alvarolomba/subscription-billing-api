create policy backend_users_access on users
    for all
    using (true)
    with check (true);

create policy backend_plans_access on plans
    for all
    using (true)
    with check (true);

create policy backend_customers_access on customers
    for all
    using (true)
    with check (true);

create policy backend_subscriptions_access on subscriptions
    for all
    using (true)
    with check (true);

create policy backend_invoices_access on invoices
    for all
    using (true)
    with check (true);
