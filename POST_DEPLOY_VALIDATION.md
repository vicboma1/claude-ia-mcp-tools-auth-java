# Post-Deploy Validation Guide

Despu√©s de desplegar en Railway, tienes varias opciones para validar que todo funciona correctamente.

## Opci√≥n 1: GitHub Actions Autom√tico (Recomendado) ≠

**Flujo:**
```
Push to main
    Ü
GitHub Actions Deploy workflow
    Ü
Deploy workflow completa
    Ü
Post-Deploy Validation workflow se dispara autom√ticamente
    Ü
Ejecuta validate-deployment.sh + test-railway.sh
    Ü
Reporta resultados en GitHub Actions
```

**Archivo:** `.github/workflows/post-deploy-validation.yml`

**Qu√© hace:**
1.  Espera 60 segundos a que Railway inicie completamente
2.  Ejecuta `validate-deployment.sh` (valida configuraci√≥n)
3.  Ejecuta `test-railway.sh` (prueba funcionalidad MCP)
4.  Reporta resultados en la secci√≥n "Actions" de GitHub

**Ventajas:**
- Autom√tico, sin intervenci√≥n manual
- Ejecuta despu√©s de cada deploy
- F√cil de ver logs en GitHub
- Se integra con tu CI/CD existente

**Ver resultados:**
```bash
# En GitHub
Settings Ü Actions Ü "Post-Deploy Validation" Ü √ltimas ejecuciones
```

---

## Opci√≥n 2: Manual Local

**Valida configuraci√≥n:**
```bash
bash validate-deployment.sh https://claude-ia-mcp-tools-java-staging.up.railway.app
```

**Prueba funcionalidad:**
```bash
bash test-railway.sh
```

---

## Opci√≥n 3: Procfile Release Process (Avanzado)

Si quieres que los tests se ejecuten **dentro del contenedor Railway** durante el deployment:

```procfile
# .github/workflows/deploy.yml
# Agregar esta l√≠nea antes de desplegar a Railway:

release: bash -c "echo 'App starting...'"
web: java -cp target/mcp-users-server-*.jar com.example.mcp.McpWebSocketServer ${PORT:-8080}
```

**Nota:** El proceso `release` se ejecuta ANTES de `web`, √∫til para migraciones o inicializaciones. No es ideal para tests porque:
- No puedo hacer curl a `localhost` (websocat intenta conectarse al servidor que est√ iniciando)
- El deployment se bloquea hasta que terminen los tests

---

## Opci√≥n 4: Endpoint de Health Check (Avanzado)

Agregar un endpoint `/health` que ejecute validaciones:

```java
// En McpWebSocketServer.java
GET /health Ü Responde 200 OK
GET /health/full Ü Ejecuta tests completos
```

Entonces Railway puede usar esto para health checks:
```bash
curl -f https://app.railway.app/health || exit 1
```

---

## Recomendaci√≥n

**Usa la Opci√≥n 1 (GitHub Actions)** porque:
-  No ralentiza el deployment
-  Ejecuta despu√©s de que el servidor est√© listo
-  Puedes ver los logs en GitHub
-  No tienes que hacerlo manualmente
-  Se integra con tu flujo actual

El workflow ya est√ creado en `.github/workflows/post-deploy-validation.yml`

Solo aseg√∫rate de que:
1. El workflow de Deploy ya existe (que s√≠ existe)
2. El trigger `workflow_run` est√ configurado correctamente
3. La URL de Railway es correcta (actualiza si es diferente)

---

## Customizaci√≥n

**Cambiar la URL de Railway:**
```yaml
# post-deploy-validation.yml l√≠nea ~37
bash validate-deployment.sh https://TU-URL-AQUI.up.railway.app
```

**Cambiar el tiempo de espera:**
```yaml
# post-deploy-validation.yml l√≠nea ~20
sleep 60  # Cambiar a 30, 120, etc.
```

**Agregar m√s validaciones:**
```yaml
# Agregar pasos adicionales en el job validate-deployment
- name: Custom validation
  run: |
    # Tu comando aqu√≠
```

---

## Troubleshooting

**Los tests fallan con "connection refused":**
- Aumenta el tiempo de espera de 60s a 120s
- Verifica que el Procfile est√© correcto
- Revisa los logs de Railway: `railway logs`

**websocat no se instala:**
- GitHub Actions usa `ubuntu-latest` que tiene `apt-get`
- Si tienes problemas, verifica la salida del workflow

**Tests pasan localmente pero fallan en GitHub Actions:**
- Puede ser diferencia de red/firewall
- Verifica la URL exacta en Railway dashboard
