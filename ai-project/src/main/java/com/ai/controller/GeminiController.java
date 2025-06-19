package com.ai.controller;




import java.io.IOException;
import java.util.Arrays;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import com.ai.secserviceImpl.UnsplashService;
import com.ai.secserviceImpl.UserDetailsImpl;

import com.ai.service.GeminiService;
import com.ai.service.HtmlStorageService;


import jakarta.servlet.http.HttpServletResponse;


import org.springframework.http.HttpStatus;


@Controller
public class GeminiController {


    
    @Autowired
    private GeminiService geminiService;

    @Autowired
private UnsplashService unsplashService;



   
 
       private final HtmlStorageService htmlStorageService;

    public GeminiController(HtmlStorageService htmlStorageService) {
        this.htmlStorageService = htmlStorageService;
    }


   // private String latestGeneratedHtml; 
 
 
private String extractKeywords(String prompt) {
    // Lowercase and basic clean-up
    String cleaned = prompt.toLowerCase().trim();

    // Remove some generic action phrases (keep it minimal)
    cleaned = cleaned.replaceAll("(?i)^(generate|create|build|design)\\s+(a|an|the)?\\s*", "");

    // Remove some website-related suffixes
    cleaned = cleaned.replaceAll("(?i)(landing\\s?page|web\\s?site|homepage|site)\\s*", "");

    // Remove trailing things like "with contact form"
    cleaned = cleaned.replaceAll("(?i)\\s*(with|including|featuring).*", "");

    // Strip punctuation
    cleaned = cleaned.replaceAll("[^a-zA-Z0-9\\s]", "");

    // Trim and collapse spaces
    cleaned = cleaned.replaceAll("\\s{2,}", " ").trim();

    // Capitalize
    String[] words = cleaned.split(" ");
    return Arrays.stream(words)
                 .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                 .collect(Collectors.joining(" "));
}


@PostMapping("/generate")
@ResponseBody
public ResponseEntity<String> generate(@RequestParam("prompt") String prompt, Authentication authentication) {

    if (prompt == null || prompt.trim().isEmpty()) {
        return ResponseEntity.badRequest().body("Prompt cannot be empty.");
    }

    // Step 1: Get authenticated user's email
    String email = "";
    Object principal = authentication.getPrincipal();

    if (principal instanceof UserDetailsImpl userDetails) {
        email = userDetails.getUser().getEmail();
    } else if (principal instanceof DefaultOAuth2User oauthUser) {
        email = oauthUser.getAttribute("email");
    } else {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Unsupported principal type: " + principal.getClass().getName());
    }

   String keywords = extractKeywords(prompt); // 🎯 Extracted core topic like "fitness studio", "college", etc.

String heroImageUrl = unsplashService.getImageUrl(keywords + " front view");
String aboutImageUrl = unsplashService.getImageUrl(keywords + " interior workspace");
String testimonialImageUrl = unsplashService.getImageUrl(keywords + " smiling customer portrait");


 String enhancedPrompt =

      "Generate a complete, production-ready HTML5 landing page for a " + prompt.trim() + " website. "
      + "Use Tailwind CSS via CDN, Google Fonts (Playfair Display for headings and Inter or Poppins for body text), and Remixicon or FontAwesome icons via CDN. "
      + "Crucially, include a custom Tailwind CSS configuration in the <script> tag for extended themes, specifically defining: "
      + "- Custom boxShadow utilities (e.g., 'custom': '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)') for soft depth. "
      + "- Custom animation keyframes (e.g., fadeIn, slideIn) and utility classes to apply subtle, elegant entrance animations (like animate-fade-in with animation-delay) to sections and elements as they appear on scroll. "
      + "The layout must include: a sticky header with refined navigation and subtle hover effects, a hero section with a compelling heading and subheading, incorporating a subtle background image with a gradient overlay for depth and visual interest, "
      + "a services section with 4–6 visually appealing feature cards, each with relevant icons, soft shadows, and smooth hover transformations, "
      + "an About section with image and descriptive text, featuring modern spacing, "
      + "a process section with 3–4 clearly defined steps, possibly with a subtle visual flow, "
      + "a testimonials section with real user avatars, quotes, and modern card styling, "
      + "a prominent call-to-action section with a distinct, well-styled button, "
      + "a contact form (name, email, message) with cleanly designed input fields and focus states, "
      + "a professional footer with social media icons and additional relevant links (e.g., Privacy Policy, Terms of Service). "
      + "Ensure the design uses responsive layouts for optimal viewing on all devices, ample whitespace for clarity, and elegant typography with precise line heights. "
      + "Use *only real, working Unsplash image URLs*. Do *not* use base64, broken URLs, placeholder SVGs, or blank image src. "
      + "If a real image URL is not available, **do not include an <img> tag at all**. Every image included must have a valid src and a meaningful alt attribute. "
      + "You may include minimal JavaScript for mobile menu toggle and smooth scrolling, ensuring it's efficient and non-obtrusive. "
      + "Return *only* the full HTML5 document from <!DOCTYPE html> to </html> — no markdown, code blocks, explanation, or comments.";    



   
    // 🔁 Step 4: Replace placeholders with actual image URLs
    enhancedPrompt = enhancedPrompt
        .replace("[HERO_IMAGE_URL]", heroImageUrl != null ? heroImageUrl : "")
        .replace("[ABOUT_IMAGE_URL]", aboutImageUrl != null ? aboutImageUrl : "")
        .replace("[TESTIMONIAL_IMAGE_URL]", testimonialImageUrl != null ? testimonialImageUrl : "");

    // 🧠 Step 5: Generate response
    String response = geminiService.generateResponse(enhancedPrompt, email);
    System.out.println("Generated HTML Length: " + response.length());

// 🧼 Step 6: Clean up markdown wrappers
response = response.replaceFirst("^\\s*```html\\s*", "")
                   .replaceFirst("^\\s*```\\s*", "")
                   .replaceFirst("```\\s*$", "")
                   .trim();

    // 🛡️ Step 7: Wrap in HTML if missing
    if (!response.contains("<html>")) {
        response = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Generated Page</title></head><body>"
                 + response
                 + "</body></html>";
    }

    // 💾 Step 8: Store HTML for later viewing
    htmlStorageService.setLatestGeneratedHtml(response);

    return ResponseEntity.ok("success");
}


private String injectClientSideDownload(String html) {
    String headerAndScript = """
        <header style="position: fixed; top: 0; left: 0; right: 0; z-index: 50; background-color: black; color: white; box-shadow: 0 2px 4px rgba(0,0,0,0.2);">
    <nav style="max-width: 1200px; margin: 0 auto; padding: 12px 16px; display: flex; justify-content: space-between; align-items: center;">
        <!-- Left: Blaze AI -->
        <div style="text-align: left;">
            <span style="font-size: 1.25rem; font-weight: bold;">Blaze AI</span>
        </div>

        <!-- Right: Download Button -->
        <div style="display: flex; align-items: center; gap: 8px; text-align: right;">
            <span style="display: none; color: #D1D5DB; font-size: 0.875rem;" class="md:inline">Download your prototype:</span>
            <button onclick="downloadHTML()" style="background-color: #16A34A; color: white; font-size: 0.875rem; padding: 6px 16px; border-radius: 6px; cursor: pointer;"
                onmouseover="this.style.backgroundColor='#15803D'"
                onmouseout="this.style.backgroundColor='#16A34A'">
                Export
            </button>
        </div>
    </nav>
</header>

<main style="padding-top: 96px;"></main>

<script>
    function downloadHTML() {
        const source = document.documentElement.outerHTML;
        const blob = new Blob([source], { type: 'text/html' });
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = 'generated-prototype.html';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(a.href);
    }
</script>
    """;

    String updatedHtml = html.replaceFirst("(?i)(<body[^>]*>)", "$1" + headerAndScript);
    updatedHtml = updatedHtml.replaceFirst("(?i)</body>", "</main></body>");
    return updatedHtml;
}





@GetMapping("/view")
public void viewGeneratedPage(HttpServletResponse response) throws IOException {
    response.setContentType("text/html;charset=UTF-8");
    String htmlContent = htmlStorageService.getLatestGeneratedHtml();
    
    String fullHtml = htmlContent != null ? injectClientSideDownload(htmlContent) : "<h2>No content generated yet.</h2>";
    response.getWriter().write(fullHtml);
}




    @GetMapping("/pro")
    public String pro()
    {
        return "pro";
    }

    @GetMapping("/pay")
    public String showPaymentPage() {
        return "pay"; // Don't include .html
    }

  

}


