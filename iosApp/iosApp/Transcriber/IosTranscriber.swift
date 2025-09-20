
//
// Created by Romeo Betances on 25/8/25.
//

import Foundation
import Speech
import AVFoundation


@objc public class IosTranscriber: NSObject {
    @objc public func transcribeFile(url: URL, languageHint: String?) async throws -> (String, String?) {

        
        let authorized = await withCheckedContinuation { cont in
            SFSpeechRecognizer.requestAuthorization { status in
                cont.resume(returning: status == .authorized)
            }
        }
        
        guard authorized else { throw NSError(domain: "Speech", code: 1) }

      



        let locale = Locale(identifier: languageHint ?? Locale.current.identifier)
        guard let recognizer = SFSpeechRecognizer(locale: locale) else { throw NSError(domain: "Speech", code: 2) }



        let inputURL = try await ensureAudioFile(url: url)


        let request = SFSpeechURLRecognitionRequest(url: inputURL)
        request.requiresOnDeviceRecognition = false // set true if device supports
        request.shouldReportPartialResults = false


        return try await withCheckedThrowingContinuation { cont in
            recognizer.recognitionTask(with: request) { result, error in
                if let error = error { cont.resume(throwing: error); return }
                if let result = result, result.isFinal {
                    cont.resume(returning: (result.bestTranscription.formattedString, recognizer.locale.identifier))
                }
            }
        }
    }



    private func ensureAudioFile(url: URL) async throws -> URL {
        let asset = AVURLAsset(url: url)
        
       // let audioTracks = try await asset.loadTracks(withMediaType: AVMediaType.audio)
       // let videoTracks = try await asset.loadTracks(withMediaType: AVMediaType.video)
        
        
        if asset.tracks(withMediaType: .audio).isEmpty { throw NSError(domain: "Speech", code: 3) }

        if asset.tracks(withMediaType: .video).isEmpty { return url }



        let outURL = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString + ".m4a")
        guard let exporter = AVAssetExportSession(asset: asset, presetName: AVAssetExportPresetAppleM4A) else {
            throw NSError(domain: "Speech", code: 4)
        }
        exporter.outputFileType = .m4a
        exporter.outputURL = outURL
        await exporter.export()
        if exporter.status != .completed { throw exporter.error ?? NSError(domain: "Speech", code: 5) }
        return outURL
    }
}

