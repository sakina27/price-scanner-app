# Use OpenJDK base
FROM openjdk:11

# Install dependencies
RUN apt-get update && apt-get install -y wget unzip git

# Install Gradle
RUN wget https://services.gradle.org/distributions/gradle-7.5-bin.zip && \
    unzip gradle-7.5-bin.zip -d /opt && \
    ln -s /opt/gradle-7.5/bin/gradle /usr/bin/gradle

# Set environment variables
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=$PATH:$ANDROID_HOME/emulator:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin:$ANDROID_HOME/platform-tools

# Create workspace
WORKDIR /workspace
COPY . .

# Build APK
RUN chmod +x ./gradlew
RUN gradle clean assembleDebug

RUN find . > /workspace/tree.txt