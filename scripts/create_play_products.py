#!/usr/bin/env python3
"""
Create Google Play one-time IAP products for com.katafract.parkarmor.

Two products mirror the iOS StoreKit catalog:
  Pro        (non-consumable unlock, $3.99)
  Tip Small  ($1.99, consumable — consumption handled app-side)

Uses the NEW monetization.onetimeproducts API (old inappproducts endpoint
is deprecated and returns 403 "Please migrate to the new publishing API").

Idempotent: `patch` has create-or-update semantics keyed on productId.

Usage:
  SERVICE_ACCOUNT_KEY=/tmp/PLAY_SERVICE_ACCOUNT_KEY.json \
      python3 create_play_products.py
"""

import os
import sys

try:
    from googleapiclient.discovery import build
    from google.oauth2 import service_account
except ImportError:
    sys.exit("pip install google-api-python-client google-auth")

PACKAGE = "com.katafract.parkarmor"

# USD price -> (units, nanos)
def usd(dollars, cents):
    return {"currencyCode": "USD", "units": str(dollars), "nanos": cents * 10_000_000}


REGIONS_VERSION = "2025/03"  # Source: monetization.convertRegionPrices().regionVersion


def eur(dollars, cents):
    # Rough 1:1 conversion for the anchor; Play auto-converts to all other regions.
    return {"currencyCode": "EUR", "units": str(dollars), "nanos": cents * 10_000_000}


def product(product_id, title, description, dollars, cents):
    """
    Globally-available one-time product. `newRegionsConfig` ensures new Play
    regions are auto-priced; `regionalPricingAndAvailabilityConfigs` anchors
    USD + EUR which the Play Console will expand to all other regions via
    automatic conversion.
    """
    return {
        "packageName": PACKAGE,
        "productId": product_id,
        "listings": [{
            "languageCode": "en-US",
            "title": title,
            "description": description,
        }],
        "purchaseOptions": [{
            "purchaseOptionId": "buy",
            "buyOption": {"legacyCompatible": True},
            "newRegionsConfig": {
                "availability": "AVAILABLE",
                "usdPrice": usd(dollars, cents),
                "eurPrice": eur(dollars, cents),
            },
            "regionalPricingAndAvailabilityConfigs": [
                {
                    "regionCode": "US",
                    "availability": "AVAILABLE",
                    "price": usd(dollars, cents),
                },
                {
                    "regionCode": "DE",
                    "availability": "AVAILABLE",
                    "price": eur(dollars, cents),
                },
            ],
            "taxAndComplianceSettings": {
                "withdrawalRightType": "WITHDRAWAL_RIGHT_DIGITAL_CONTENT",
            },
        }],
    }


PRODUCTS = [
    product(
        "com.katafract.parkarmor.pro",
        "ParkArmor Pro",
        "One-time Pro unlock: unlimited parking history, home screen widget, and parking sign photos.",
        3, 99,
    ),
    product(
        "com.katafract.parkarmor.tip.small",
        "Small Tip",
        "A small tip to support ParkArmor development. Thank you!",
        1, 99,
    ),
]


def main():
    key_path = os.environ.get("SERVICE_ACCOUNT_KEY", "/tmp/PLAY_SERVICE_ACCOUNT_KEY.json")
    if not os.path.exists(key_path):
        sys.exit(f"Service account key not found at {key_path}")

    creds = service_account.Credentials.from_service_account_file(
        key_path, scopes=["https://www.googleapis.com/auth/androidpublisher"])
    svc = build("androidpublisher", "v3", credentials=creds, cache_discovery=False)
    otp = svc.monetization().onetimeproducts()

    for prod in PRODUCTS:
        pid = prod["productId"]
        print(f"\nPatching {pid} ...")
        try:
            resp = otp.patch(
                packageName=PACKAGE,
                productId=pid,
                regionsVersion_version=REGIONS_VERSION,
                updateMask="listings,purchaseOptions,taxAndComplianceSettings",
                allowMissing=True,
                body=prod,
            ).execute()
            price = resp["purchaseOptions"][0]["regionalPricingAndAvailabilityConfigs"][0]["price"]
            print(f"  OK — {resp['listings'][0]['title']} ${price['units']}.{str(price['nanos'])[:2]}")
        except Exception as e:
            print(f"  ERROR: {str(e)[:400]}")

    print("\nActivate purchase options in Play Console → Products → In-app products")
    print("if the product state shows DRAFT after creation.")


if __name__ == "__main__":
    main()
