# Safaricom M-Pesa Connector

Part of **Mifos Payment Hub**, this is a **microservice** that enables integration with **Safaricom M-Pesa** for processing payments, transactions, and collections. It handles all communication with the M-Pesa API and can be used in sandbox or production environments.

## Features
- Initiate **C2B (Customer to Business)** and **B2C (Business to Customer)** transactions
- Handle **transaction status callbacks** and **confirmations**
- Works with **Daraja API** sandbox for testing
- Can be integrated with other Payment Hub modules like `ph-ee-connector-common`

## Requirements
- Java 11 (for most modules) or Java 17 (for `ph-ee-connector-common`)
- Gradle build system
- Local Maven repo for SNAPSHOT dependencies

## Safaricom Documentation
- [Daraja API Documentation](https://developer.safaricom.co.ke)

## Contributing

We welcome contributions! Please read our [contribution guidelines](./CONTRIBUTING.md) before submitting pull requests.

## Related Projects

[Apache Fineract](https://github.com/apache/fineract) - Apache Fineract® provides open APIs and affordable core banking solution for financial institutions and is the backend for all UIs of the Mifos®.
