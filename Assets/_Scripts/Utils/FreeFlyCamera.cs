using UnityEngine;
using UnityEngine.InputSystem; // Yeni Input Sistemi'ni kullanıyoruz

/*
 * BASİT SERBEST KAMERA KONTROLCÜSÜ (FLYCAM)
 * * WASD tuşları ile hareket eder.
 * * Fare ile etrafa bakar.
 * * Shift tuşu ile hızı artırır.
 * * Bu script, sahnedeki Main Camera objesine eklenmelidir.
 */
public class FreeFlyCamera : MonoBehaviour
{
    [Header("Hareket Ayarları")]
    [SerializeField] private float moveSpeed = 5.0f;
    [SerializeField] private float shiftMultiplier = 2.0f; // Shift'e basınca hız çarpanı

    [Header("Bakış Ayarları")]
    [SerializeField] private float lookSensitivity = 2.0f;
    [SerializeField] private bool lockCursor = true; // Oyun başladığında fare imlecini gizle/kilitle

    private float rotationX = 0.0f;
    private float rotationY = 0.0f;

    void Start()
    {
        if (lockCursor)
        {
            Cursor.lockState = CursorLockMode.Locked;
            Cursor.visible = false;
        }
        // Başlangıç rotasyonunu kameranın mevcut açısına ayarla
        Vector3 startRotation = transform.eulerAngles;
        rotationX = startRotation.y;
        rotationY = -startRotation.x; // Unity'de Y ekseni dikey bakış açısını kontrol eder (pitch)
    }

    void Update()
    {
        // --- Bakış Kontrolü (Fare) ---
        if (Mouse.current != null)
        {
            Vector2 mouseDelta = Mouse.current.delta.ReadValue() * lookSensitivity * Time.deltaTime * 50f; // Delta time ile hassasiyeti ayarla
            rotationX += mouseDelta.x;
            rotationY -= mouseDelta.y;
            rotationY = Mathf.Clamp(rotationY, -90f, 90f); // Dikey bakış açısını sınırla

            transform.localRotation = Quaternion.Euler(-rotationY, rotationX, 0f);
        }

        // --- Hareket Kontrolü (Klavye) ---
        if (Keyboard.current != null)
        {
            float currentSpeed = moveSpeed;
            if (Keyboard.current.leftShiftKey.isPressed)
            {
                currentSpeed *= shiftMultiplier;
            }

            Vector3 moveDirection = Vector3.zero;
            if (Keyboard.current.wKey.isPressed) moveDirection += transform.forward;
            if (Keyboard.current.sKey.isPressed) moveDirection -= transform.forward;
            if (Keyboard.current.aKey.isPressed) moveDirection -= transform.right;
            if (Keyboard.current.dKey.isPressed) moveDirection += transform.right;
            if (Keyboard.current.spaceKey.isPressed) moveDirection += Vector3.up; // Yukarı hareket (Opsiyonel)
            if (Keyboard.current.leftCtrlKey.isPressed) moveDirection -= Vector3.up; // Aşağı hareket (Opsiyonel)


            transform.position += moveDirection.normalized * currentSpeed * Time.deltaTime;
        }

        // --- İmleci Açma/Kapama ---
        if (Keyboard.current != null && Keyboard.current.escapeKey.wasPressedThisFrame)
        {
             if (Cursor.lockState == CursorLockMode.Locked)
             {
                 Cursor.lockState = CursorLockMode.None;
                 Cursor.visible = true;
             } else {
                  Cursor.lockState = CursorLockMode.Locked;
                  Cursor.visible = false;
             }
        }
    }
}