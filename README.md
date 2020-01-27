# kafka-wordsnake [![Codacy Badge](https://api.codacy.com/project/badge/Grade/c8e0e5b909974bf0a7db8c36d4e9e391)](https://www.codacy.com/app/BekyP/kafka-wordsnake?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=BekyP/kafka-wordsnake&amp;utm_campaign=Badge_Grade) [![Codacy Badge](https://api.codacy.com/project/badge/Coverage/c8e0e5b909974bf0a7db8c36d4e9e391)](https://www.codacy.com/app/BekyP/kafka-wordsnake?utm_source=github.com&utm_medium=referral&utm_content=BekyP/kafka-wordsnake&utm_campaign=Badge_Coverage) ![Java CI](https://github.com/BekyP/kafka-wordsnake/workflows/Java%20CI/badge.svg?branch=master)

- application for creating wordsnakes using kafka and kafka streams:

![wordsnake](readme-wordsnake.jpg)

- wordsnakes are consumed from output topic and written to file on disk

## project structure

*src* - source codes of java application

*utils* - utils scripts for docker kafka-env (create kafka topics, send dummy data, etc.)

*run.sh*  - sets up kafka-env from docker-compose.yml, creates default topics, fill dummy data, builds java app (jar) and simple docker image (using Dockerfile), runs app image and starts processing data from/to default topics
