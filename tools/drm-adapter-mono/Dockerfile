FROM mono:6.12

WORKDIR /app
COPY Program.cs /app/Program.cs
COPY drm.edi.dll /app/drm.edi.dll

RUN mcs -out:/app/DrmBridge.exe \
    -reference:/app/drm.edi.dll \
    -r:System.Net.Http.dll \
    -r:System.Web.Extensions.dll \
    Program.cs

EXPOSE 8081
ENTRYPOINT ["mono", "/app/DrmBridge.exe"]
