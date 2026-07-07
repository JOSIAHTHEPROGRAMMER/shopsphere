# Security Policy

## Overview

ShopSphere is a personal portfolio project built to demonstrate backend engineering practices with Spring Boot. It is not a production deployment and does not currently handle real customer data, real payments, or real personal information. That said, the codebase implements genuine security practices including password hashing, JWT based authentication, and role based authorization, and this document explains the current security posture along with its known limitations.

## Supported Versions

This project has a single active branch. Only the latest commit on the main branch receives fixes. There are no maintained release versions or long term support branches at this stage.

## Reporting a Vulnerability

If you find a security issue in this project, please open an issue on the GitHub repository or reach out directly through GitHub. Since this is a personal, non production project, there is no dedicated security contact or bug bounty program. Reports will be reviewed and addressed on a best effort basis.

Please avoid publicly disclosing details of a serious vulnerability until it has been reviewed.

## Current Security Measures

- Passwords are hashed using BCrypt before storage. Plaintext passwords are never persisted.
- Authentication is handled through JWT bearer tokens with a signed secret and an expiration window.
- Authorization is enforced through Spring Security using role based access control for Customer, Seller, and Admin roles.
- Ownership checks are enforced on user and order resources so that one account cannot access or modify another account's data without the appropriate role.
- Request bodies are validated using Jakarta Bean Validation before reaching business logic.
- Application errors are handled through a centralized exception handler that avoids leaking internal stack traces or implementation details to API consumers.
- Sensitive values such as the JWT signing secret are stored outside of version control using environment variables and a local only Spring profile.
- Logged authentication failures mask the associated email address rather than recording it in full.

## Known Limitations

This project is under active development and the following limitations are known and accepted for its current stage:

- The application uses an in memory H2 database that resets on restart. It is not intended to persist real data over time.
- There is currently no mechanism to change a user's role through the API. Role assignment is handled directly through seed data or manual database updates.
- Product visibility filtering allows an authenticated customer to technically request inactive products through query parameters. This is accepted for the current single seller scope of the project.
- There is no rate limiting on authentication endpoints at this stage.
- The project has not undergone third party security auditing or penetration testing.

## Dependency Management

Dependencies are managed through Maven and are periodically reviewed for updates, including the Spring Boot version, the JWT library, and Spring Security. If a dependency is found to carry a known vulnerability, it will be updated as part of ongoing maintenance.

## Disclaimer

This project is provided for educational and portfolio purposes. It should not be deployed in a production environment or used to handle real user data without a thorough security review beyond what is described here.
