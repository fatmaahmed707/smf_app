# NFC Site — Worker Medical Lookup

Call the `get_worker_medical` RPC with the worker's UUID (from the NFC tag URL):

```
POST https://bsowsdicxlvfvwwsabhz.supabase.co/rest/v1/rpc/get_worker_medical
Content-Type: application/json
apikey: sb_publishable_27AUS1VZktlYYZUBkguWCw_J75T1cUp

{
  "worker_uuid": "<UUID>"
}
```

### Response

```json
[
  {
    "full_name_en": "Ahmed Mahmoud Abdelrahman",
    "full_name_ar": "أحمد محمود عبد الرحمن",
    "medical_condition_en": "Type 2 Diabetes",
    "medical_condition_ar": "مرض السكري - النوع الثاني",
    "clinical_notes_en": "Requires regular glucose monitoring & oral meds.",
    "clinical_notes_ar": "يتطلب مراقبة دورية للسكر وأدوية فموية",
    "emergency_contact_name": "Sarah Khaled",
    "emergency_contact_relation": "Wife",
    "emergency_phone": "01198765432"
  }
]
```

Empty array `[]` means the worker was not found.
