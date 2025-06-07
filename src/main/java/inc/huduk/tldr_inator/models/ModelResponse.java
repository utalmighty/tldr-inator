package inc.huduk.tldr_inator.models;

public record ModelResponse(String model, String created_at, String response, String done, String done_reason,
                            Long[] context, Long total_duration, Long load_duration, Long prompt_eval_count,
                            Long prompt_eval_duration, Long eval_count, Long eval_duration) {
}
