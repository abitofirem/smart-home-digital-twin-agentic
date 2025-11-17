using UnityEngine;
using System; 

namespace SmartHome.Core
{
    public class SimulationManager : MonoBehaviour
    {
        #region Singleton
        public static SimulationManager Instance { get; private set; }

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Debug.LogWarning("[SimulationManager] Sahnede birden fazla SimulationManager bulundu! Bu örnek yok ediliyor.");
                Destroy(gameObject);
                return;
            }
            Instance = this;
        }
        #endregion

        // --- DEĞİŞKEN TANIMLAMALARI BURADA BAŞLIYOR (YORUM DEĞİL!) ---
        [Header("Zaman Simülasyonu")]
        [Tooltip("Simülasyondaki 1 saniyenin, gerçek dünyadaki kaç saniyeye denk geldiği.")]
        [SerializeField] private float timeScaleMultiplier = 60f; 
        [Tooltip("Simülasyonun başlangıç saati (0-24 arası).")]
        [SerializeField] [Range(0f, 24f)] private float startTimeOfDay = 8.0f; 

        [Header("Dış Hava Sıcaklığı Simülasyonu")]
        [Tooltip("Günün en soğuk saati (0-24 arası).")]
        [SerializeField] [Range(0f, 24f)] private float coldestHour = 4.0f;
        [Tooltip("Günün en sıcak saati (0-24 arası).")]
        [SerializeField] [Range(0f, 24f)] private float warmestHour = 14.0f;
        [Tooltip("Minimum dış hava sıcaklığı (°C).")]
        [SerializeField] private float minTemperature = 10.0f;
        [Tooltip("Maksimum dış hava sıcaklığı (°C).")]
        [SerializeField] private float maxTemperature = 25.0f;
        
        [Header("Görsel Zaman Döngüsü")]
        [Tooltip("Sahnedeki ana ışık kaynağı (Güneş). Inspector'dan atanmalı.")]
        [SerializeField] private Light sunLight; 
        [Tooltip("Güneşin en tepedeyken (öğlen) sahip olacağı renk.")]
        [SerializeField] private Color sunColorNoon = Color.white;
        [Tooltip("Güneşin doğuş/batış anındaki rengi.")]
        [SerializeField] private Color sunColorHorizon = new Color(1.0f, 0.7f, 0.4f); 
        [Tooltip("Güneşin en tepedeyken (öğlen) sahip olacağı yoğunluk.")]
        [SerializeField] [Range(0f, 5f)] private float sunMaxIntensity = 3.0f;
        [Tooltip("Güneş battıktan sonraki (gece) minimum yoğunluğu.")]
        [SerializeField] [Range(0f, 1f)] private float sunMinIntensity = 0.1f;
        [Tooltip("Ortam ışığının gündüz rengi (gökyüzü rengi gibi).")]
        [SerializeField] private Color ambientColorDay = new Color(0.2f, 0.3f, 0.4f); 
        [Tooltip("Ortam ışığının gece rengi.")]
        [SerializeField] private Color ambientColorNight = Color.black;
        [Tooltip("Ortam ışığının gündüz maksimum yoğunluğu.")]
        [SerializeField] [Range(0f, 2f)] private float ambientMaxIntensity = 1.0f;
         [Tooltip("Ortam ışığının gece minimum yoğunluğu.")]
        [SerializeField] [Range(0f, 1f)] private float ambientMinIntensity = 0.1f;
        // --- DEĞİŞKEN TANIMLAMALARI BURADA BİTİYOR ---

        public float CurrentHourOfDay { get; private set; }
        public float CurrentExternalTemperature { get; private set; }

        private void Start()
        {
            CurrentHourOfDay = startTimeOfDay; // Artık hata vermemeli
            UpdateTemperature(); 
            UpdateLighting(); 
            Debug.Log($"[SimulationManager] Başlatıldı. Zaman Ölçeği: {timeScaleMultiplier}x, Başlangıç Saati: {CurrentHourOfDay:F2}, Başlangıç Sıcaklığı: {CurrentExternalTemperature:F1}°C"); // Artık hata vermemeli

            if (sunLight == null) { Debug.LogError("[SimulationManager] HATA: 'Sun Light' atanmamış!", this); }
            if(RenderSettings.ambientMode != UnityEngine.Rendering.AmbientMode.Flat) { Debug.LogWarning("[SimulationManager] En iyi sonuç için Lighting > Environment > Environment Lighting > Source'ı 'Color' yapın.", this); }
        }

        private void Update()
        {
            CurrentHourOfDay += (Time.deltaTime * timeScaleMultiplier) / 3600.0f; // Artık hata vermemeli
            if (CurrentHourOfDay >= 24.0f) CurrentHourOfDay -= 24.0f;

            UpdateTemperature();
            UpdateLighting(); 
        }

        private void UpdateTemperature()
        {
            float dayProgress = CurrentHourOfDay / 24.0f; 
            float sineValue = Mathf.Sin((float)(Math.PI * 2.0 * dayProgress - Math.PI / 2.0)); 
            float normalizedTemp = (sineValue + 1.0f) / 2.0f; 
            CurrentExternalTemperature = Mathf.Lerp(minTemperature, maxTemperature, normalizedTemp); // Artık hata vermemeli
        }

        private void UpdateLighting()
        {
            if (sunLight == null) return; 

            float dayProgress = CurrentHourOfDay / 24.0f; 
            float sunAngleFactor = Mathf.Clamp01(Mathf.Sin((float)Math.PI * dayProgress)); 

            sunLight.intensity = Mathf.Lerp(sunMinIntensity, sunMaxIntensity, sunAngleFactor);
            sunLight.color = Color.Lerp(sunColorHorizon, sunColorNoon, sunAngleFactor);
            
            float sunRotationX = 180f * sunAngleFactor; 
            float sunRotationY = Mathf.Lerp(0f, 180f, dayProgress); 
            sunLight.transform.rotation = Quaternion.Euler(sunRotationX - 90f, sunRotationY, 0f); 

            RenderSettings.ambientIntensity = Mathf.Lerp(ambientMinIntensity, ambientMaxIntensity, sunAngleFactor);
            RenderSettings.ambientLight = Color.Lerp(ambientColorNight, ambientColorDay, sunAngleFactor);
        }
    }
}