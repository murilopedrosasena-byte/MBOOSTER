import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ScrollView;
import android.widget.TextView;
import rikka.shizuku.Shizuku;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends Activity {

    private TextView logText;
    private ScrollView scrollView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button btnLimparCache = findViewById(R.id.btnLimparCache);
        Button btnOtimizarBateria = findViewById(R.id.btnOtimizarBateria);
        Button btnLimparRAM = findViewById(R.id.btnLimparRAM);
        Button btnCheckShizuku = findViewById(R.id.btnCheckShizuku);
        logText = findViewById(R.id.logText);
        scrollView = findViewById(R.id.scrollView);
        
        Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
        
        btnCheckShizuku.setOnClickListener(v -> checkShizuku());
        btnLimparCache.setOnClickListener(v -> limparCache());
        btnOtimizarBateria.setOnClickListener(v -> otimizarBateria());
        btnLimparRAM.setOnClickListener(v -> limparRAM());
    }
    
    private void log(String msg) {
        runOnUiThread(() -> {
            logText.append(msg + "\n");
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        });
    }
    
    private void checkShizuku() {
        if (checkShizukuPermission()) {
            log("✅ Shizuku está funcionando!");
            Toast.makeText(this, "Shizuku OK!", Toast.LENGTH_SHORT).show();
        } else {
            log("❌ Shizuku não disponível");
            log("→ Abra o app Shizuku e conceda permissão");
            requestShizukuPermission();
        }
    }
    
    private boolean checkShizukuPermission() {
        if (Shizuku.pingBinder()) {
            if (Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }
    
    private void requestShizukuPermission() {
        if (Shizuku.pingBinder()) {
            if (Shizuku.checkSelfPermission() != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(0);
            }
        }
    }
    
    private final Shizuku.OnRequestPermissionResultListener REQUEST_PERMISSION_RESULT_LISTENER = (requestCode, grantResult) -> {
        if (grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            log("✅ Permissão Shizuku concedida!");
        } else {
            log("❌ Permissão Shizuku negada");
        }
    };
    
    private void limparCache() {
        log("🧹 Limpando cache...");
        executeShizukuCommand("pm trim-caches 999G");
    }
    
    private void otimizarBateria() {
        log("🔋 Otimizando bateria...");
        executeShizukuCommand("cmd power set-adaptive-power-saver-enabled true");
        executeShizukuCommand("settings put global battery_saver_constants vibration_disabled=true");
    }
    
    private void limparRAM() {
        log("🚀 Limpando RAM...");
        executeShizukuCommand("am kill-all");
    }
    
    private void executeShizukuCommand(String command) {
        if (!checkShizukuPermission()) {
            log("❌ Sem permissão Shizuku!");
            requestShizukuPermission();
            return;
        }
        
        new Thread(() -> {
            try {
                Process process = Shizuku.newProcess(new String[]{"sh", "-c", command}, null, null);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    log("→ " + line);
                }
                int exitCode = process.waitFor();
                log("✅ Comando executado (code: " + exitCode + ")");
            } catch (Exception e) {
                log("❌ Erro: " + e.getMessage());
            }
        }).start();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
    }
}
