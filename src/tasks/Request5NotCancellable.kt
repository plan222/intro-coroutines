package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

suspend fun loadContributorsNotCancellable(service: GitHubService, req: RequestData): List<User> = coroutineScope {
    val repos: List<Repo> = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    return@coroutineScope repos.map { repo ->
        GlobalScope.async {
            log("starting loading for ${repo.name}")
            delay(3000)
            service
                .getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .body() ?: emptyList()
        }
    }
        .awaitAll()
        .flatten()
        .aggregate()
}
