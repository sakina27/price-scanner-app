FROM openjdk:11

# Install required packages
RUN apt-get update && apt-get install -y wget unzip git

# Install Gradle
RUN wget https://services.gradle.org/distributions/gradle-7.5-bin.zip && \
    unzip gradle-7.5-bin.zip -d /opt/ && \
    ln -s /opt/gradle-7.5/bin/gradle /usr/bin/gradle

ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=$PATH:$ANDROID_HOME/emulator:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin:$ANDROID_HOME/platform-tools


# Install Android command line tools
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    wget https://dl.google.com/android/repository/commandlinetools-linux-8512546_latest.zip -O tools.zip && \
    unzip tools.zip -d ${ANDROID_HOME}/cmdline-tools && \
    mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest

# Accept licenses and install necessary SDK components
RUN yes | ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager --licenses && \
    ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager \
        "platform-tools" \
        "platforms;android-30" \
        "build-tools;30.0.3"

# Set working directory inside container
WORKDIR /workspace

# Copy project files into container
COPY . .

RUN apt-get install -y dos2unix && dos2unix ./gradlew


# Build APK
RUN ./gradlew clean assembleDebug

# Optional: list output for debugging
RUN find . > /workspace/tree.txt

# Final command (can be overridden)
CMD ["sh"]
