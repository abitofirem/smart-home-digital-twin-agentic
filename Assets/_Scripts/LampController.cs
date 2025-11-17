using UnityEngine;
using UnityEngine.InputSystem;
using System.Collections; // Gerekli değil ama iyi bir alışkanlık

public class LampController : MonoBehaviour
{
    [Header("Veritabanı Kimliği")]
    public int deviceId = 1;

    [Header("Bağlantılar")]
    [SerializeField] private Light lampLight;
    // --- DEĞİŞİKLİK 1: Tekil Renderer yerine Renderer dizisi (array) kullanıyoruz ---
    [SerializeField] private Renderer[] lampRenderers; 

    [Header("Durum")]
    [SerializeField] private bool isOn = false;

    void Awake()
    {
        // Bu otomatik atamalar, Inspector'da bir şey unutulursa diye kullanışlıdır.
        if (lampLight == null) lampLight = GetComponentInChildren<Light>();
        // lampRenderers dizisi manuel olarak atanacağı için otomatik atama satırını kaldırıyoruz.
        ApplyState(isOn);
    }

    void OnEnable()
    {
        MqttController.OnMessageArrived += HandleMqttMessage;
    }

    void OnDisable()
    {
        MqttController.OnMessageArrived -= HandleMqttMessage;
    }

    private void HandleMqttMessage(string message)
    {
        DeviceStatus status = JsonUtility.FromJson<DeviceStatus>(message);
        if (status != null && status.deviceId == this.deviceId)
        {
            Debug.Log($"[LampController] ID:{deviceId} için mesaj alındı: {message}");
            SetState(status.newStatus.ToLower() == "on");
        }
    }

    public void SetState(bool on)
    {
        if (isOn == on) return; // Zaten aynı durumdaysa bir şey yapma

        isOn = on;
        ApplyState(isOn);
        Debug.Log($"[LampController] Lamp ID:{deviceId} -> {(isOn ? "ON" : "OFF")}");
    }

    // --- DEĞİŞİKLİK 2: Fonksiyonu, dizideki her bir renderer için çalışacak şekilde güncelliyoruz ---
    private void ApplyState(bool on)
    {
        if (lampLight != null) lampLight.enabled = on;

        // Dizinin boş olmadığından emin ol
        if (lampRenderers != null && lampRenderers.Length > 0)
        {
            // Dizideki her bir renderer (r) için aşağıdaki işlemleri yap
            foreach (Renderer r in lampRenderers)
            {
                if (r != null && r.material != null)
                {
                    if (on)
                    {
                        // Materyalin emisyonunu (parlamasını) etkinleştir
                        r.material.EnableKeyword("_EMISSION");
                        r.material.SetColor("_EmissionColor", Color.yellow * 1.2f);
                    }
                    else
                    {
                        // Materyalin emisyonunu kapat
                        r.material.SetColor("_EmissionColor", Color.black);
                        r.material.DisableKeyword("_EMISSION");
                    }
                }
            }
        }
    }

    // --- Test Fonksiyonları ---
    public void Toggle() { SetState(!isOn); }
    
    // Klavyeden kontrol için InputSystem kullanıyoruz
    void Update() 
    { 
        if (Keyboard.current != null && Keyboard.current.lKey.wasPressedThisFrame) 
        {
            Toggle(); 
        }
    }
}