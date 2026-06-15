-- Update seed plan data with PKR pricing (INR equivalent ~280 PKR/USD)
UPDATE workspace_plans SET
    monthly_price_pkr = 0,
    yearly_price_pkr = 0,
    currency = 'USD'
WHERE code = 'FREE';

UPDATE workspace_plans SET
    monthly_price_pkr = 8120,
    yearly_price_pkr = 81200,
    currency = 'USD'
WHERE code = 'PRO';

UPDATE workspace_plans SET
    monthly_price_pkr = 27720,
    yearly_price_pkr = 277200,
    currency = 'USD'
WHERE code = 'BUSINESS';

UPDATE workspace_plans SET
    monthly_price_pkr = 83720,
    yearly_price_pkr = 837200,
    currency = 'USD'
WHERE code = 'ENTERPRISE';
