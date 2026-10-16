# Veryfi Lens Android — Headless Receipts — callbacks

Source of truth: Veryfi Lens Android SDK **2.7.0.6**, commit `7f33726c`. Names below were read from that tree. Items not fully confirmed are marked **Unconfirmed**. Do not copy repository passwords, keystores, or Demo-only types into the host app.


Delegate: `com.veryfi.lens.headless.receipts.VeryfiLensHeadlessDelegate`.

| Method | Payload |
|--------|---------|
| `veryfiLensUpdate` | `rectangles`: array of `{corner0..corner3: {x, y}}` in frame coordinates (`ImageProcessorHeadless` keys `rectangles`, `x`, `y`) |
| `veryfiLensSuccess` | Top-level `session_id`, `file_paths` (local cropped paths), `is_blurred`, `is_cropped`, `is_document_detected` |
| `veryfiLensError` | `error` string. Processor-missing path uses `Error with credentials` |
| `veryfiLensClose` | Close JSON from the processor |

Callbacks are posted to the main looper. If success JSON contains a string `data` field, it is parsed into a `JSONObject`. The capture payload puts `file_paths` at the top level.

`NotifyServerHelper.notifyServerOfCaptureReady` POSTs that same JSON under `data`, with `data_extraction_engine` = `mobile`, `document_type` receipt, and `sdk` = `lens-headless-receipts`. Image bytes are not uploaded. The host uploads `file_paths` if extraction is required.

There is no `package_id` / `failed` upload sequence on this delegate. Those belong to the UI SDK.

