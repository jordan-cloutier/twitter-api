package com.team_1.demo.services.impl;

import com.team_1.demo.dtos.*;
import com.team_1.demo.entities.Credentials;
import com.team_1.demo.entities.Hashtag;
import com.team_1.demo.entities.Tweet;
import com.team_1.demo.entities.User;
import com.team_1.demo.exceptions.BadRequestException;
import com.team_1.demo.exceptions.NotAuthorizedException;
import com.team_1.demo.exceptions.NotFoundException;
import com.team_1.demo.mappers.CredentialsMapper;
import com.team_1.demo.mappers.HashtagMapper;
import com.team_1.demo.mappers.UserMapper;
import com.team_1.demo.repositories.HashtagRepository;
import com.team_1.demo.repositories.UserRepository;
import com.team_1.demo.entities.Tweet;
import lombok.AllArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.team_1.demo.mappers.TweetMapper;
import com.team_1.demo.repositories.TweetRepository;
import com.team_1.demo.services.TweetService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TweetServiceImpl implements TweetService {

    private final TweetRepository tweetRepository;
    private final TweetMapper tweetMapper;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final HashtagRepository hashtagRepository;
    private final HashtagMapper hashtagMapper;


    @Override
    public List<TweetResponseDto> getAllTweets() {
        return tweetMapper.entitiesToResponseDtos(tweetRepository.getTweetsNotDeleted());
    }

    @Override
    public TweetResponseDto getTweet(Long id) {
        Optional<Tweet> foundTweet = tweetRepository.findById(id);
        if (foundTweet.isEmpty()) {
            throw new BadRequestException("No Tweet Found with this ID");
        }

        return tweetMapper.entityToResponseDto(foundTweet.get());
    }

    @Override
    public TweetResponseDto createTweet(TweetRequestDto tweetRequestDto) {
        if (tweetRequestDto.getContent() == null || tweetRequestDto.getCredentials() == null) {
            throw new BadRequestException("Please provide content and credentials");
        }

        User current = userRepository.findByUsername(tweetRequestDto.getCredentials().getUsername());

        validateUser(current, tweetRequestDto.getCredentials());

        Tweet newTweet = tweetMapper.requestDtoToEntity(tweetRequestDto);
        newTweet.setAuthor(current);
        newTweet.setHashtags(new ArrayList<>());
        newTweet.setMentionedUsers(new ArrayList<>());
        Tweet savedTweet = tweetRepository.saveAndFlush(newTweet);
        String[] words = newTweet.getContent().split(" ");

        for (String s : words) {
            if (s.substring(0, 1).equals("@") && userRepository.findByUsername(s.substring(1, s.length())) != null) {
                savedTweet.getMentionedUsers().add((userRepository.findByUsername(s.substring(1, s.length()))));
            }

            if (s.substring(0, 1).equals("#")) {
                Hashtag h = hashtagRepository.findByLabel(s.substring(1, s.length()));
                if (h == null) {
                    HashtagDto newHashtag = new HashtagDto();
                    newHashtag.setLabel(s.substring(1, s.length()));
                    savedTweet.getHashtags().add(hashtagRepository.saveAndFlush(hashtagMapper.hashtagDtoToEntity(newHashtag)));
                }
                savedTweet.getHashtags().add(h);
            }
        }
        return tweetMapper.entityToResponseDto(tweetRepository.saveAndFlush(savedTweet));

    }

    @Override
    public TweetResponseDto deleteTweet(Long id, CredentialsDto credentialsDto) {
        Optional<Tweet> foundTweet = tweetRepository.findById(id);
        if (foundTweet.isEmpty() || foundTweet.get().isDeleted()) {
            throw new NotFoundException("No tweet found for that ID");
        }

        User current = userRepository.findByUsername(credentialsDto.getUsername());
        validateUser(current, credentialsDto);

        foundTweet.get().setDeleted(true);
        return tweetMapper.entityToResponseDto(tweetRepository.saveAndFlush(foundTweet.get()));
    }

    @Override
    public void likedTweet(Long id, CredentialsDto credentialsDto) {
        Optional<Tweet> foundTweet = tweetRepository.findById(id);
        if (foundTweet.isEmpty() || foundTweet.get().isDeleted()) {
            throw new NotFoundException("No tweet found for that ID, cannot like");
        }

        User current = userRepository.findByUsername(credentialsDto.getUsername());
        if (foundTweet.get().getLiked_by_users().contains(current)) {
            return;
        }
        validateUser(current, credentialsDto);
        foundTweet.get().getLiked_by_users().add(current);
        current.getLiked_tweets().add(foundTweet.get());
        userRepository.saveAndFlush(current);

    }

    @Override
    public ContextDto getContext(Long id) {
        Optional<Tweet> tweet = tweetRepository.findById(id);
        validateTweet(tweet.get());
        ContextDto contextDto = new ContextDto();
        contextDto.setAfter(new ArrayList<>());
        contextDto.setBefore(new ArrayList<>());
        contextDto.setTarget(tweet.get());

        List<Tweet> loopTweets = new ArrayList<>();
        List<Tweet> innerLoopTweets;
        loopTweets.add(tweet.get());

        while (!loopTweets.isEmpty()) {
            System.out.println("while loop, list size is: " + loopTweets.size());
            for(Tweet t : loopTweets) {
                System.out.println("We're inside the first for loop!");
                if(!t.getReplies().isEmpty()) {
                    System.out.println("condition met!");
                    for(Tweet r : t.getReplies()) {
                        if(!r.isDeleted()) {
                            System.out.println("WE GOT HERE");
                            contextDto.getAfter().add(tweetMapper.entityToResponseDto(r));
                        }
                    }
                    loopTweets = t.getReplies();
                }

            }
            loopTweets =
        }

        Tweet beforeTweet = tweet.get().getInReplyTo();
        while (beforeTweet.getInReplyTo() != null) {
            System.out.println("SECOND loop");
            contextDto.getBefore().add(tweetMapper.entityToResponseDto(beforeTweet));
            beforeTweet = beforeTweet.getInReplyTo();
        }


//        List<Tweet> fullContext = new ArrayList<>();
//        List<Tweet> before = new ArrayList<>();
//        Tweet beforeCheck = tweet.getInReplyTo();
//        while(beforeCheck != null) {
//            if(!beforeCheck.isDeleted()) {
//                before.add(beforeCheck);
//            }
//            beforeCheck = beforeCheck.getInReplyTo();
//        }
//        fullContext.addAll(before);
//        fullContext.add(tweet);
//        List<Tweet> after = tweet.getReplies();
//        for(Tweet t : after) {
//            if(t.isDeleted()) {
//                after.remove(t);
//            }
//        }
//        fullContext.addAll(after);
        return contextDto;
    }

    @Override
    public TweetResponseDto repostTweet(Long id, CredentialsDto credentials) {
        User current = userRepository.findByUsername(credentials.getUsername());
        validateUser(current, credentials);

        Optional<Tweet> repostTweet = tweetRepository.findById(id);
        if (repostTweet.isEmpty()) {
            throw new BadRequestException("Tweet not found");
        }

        Tweet newTweet = new Tweet();
        newTweet.setAuthor(current);
        newTweet.setRepostOf(repostTweet.get());
        return tweetMapper.entityToResponseDto(tweetRepository.saveAndFlush(newTweet));
    }

    @Override
    public List<UserResponseDto> getTweetLikes(Long id) {
        Tweet tweet = tweetRepository.getReferenceById(id);
        validateTweet(tweet);
        List<User> users = tweet.getLiked_by_users();
        for(User u: users) {
            if(u.isDeleted()) {
                users.remove(u);
            }
        }
        return userMapper.entitiesToResponseDtos(users);
    }

    @Override
    public List<TweetResponseDto> getReplies(Long id) {
        Tweet tweet = tweetRepository.getReferenceById(id);
        validateTweet(tweet);
        List<Tweet> replies = tweet.getReplies();
        for(Tweet t : replies) {
            if(t.isDeleted()) {
                replies.remove(t);
            }
        }
        return tweetMapper.entitiesToResponseDtos(replies);
    }

    @Override
    public List<HashtagDto> getTags(Long id) {
        if (tweetRepository.findById(id).isEmpty()) {
            throw new BadRequestException("Tweet not found");
        }
        Tweet tweet = tweetRepository.getReferenceById(id);
        validateTweet(tweet);
        return hashtagMapper.entitiesToResponseDtos(tweet.getHashtags());
    }

    private void validateTweet(Tweet tweet) {
        if (tweet == null || tweet.isDeleted()) {
            throw new NotFoundException("Tweet does not exist");
        }
    }

    private void validateUser(User user, CredentialsDto credentials) {
        if (user == null) {
            throw new NotFoundException("No user found");
        }

        if (!user.getCredentials().getPassword().equals(credentials.getPassword())) {
            throw new NotAuthorizedException("Please provide required credentials");
        }
    }


    @Override
    public TweetResponseDto createReplyToTweet(Long id, TweetRequestDto tweetRequestDto) {
        Optional<Tweet> originalTweetOptional = tweetRepository.findById(id);

        if (originalTweetOptional.isEmpty()) {
            throw new NotFoundException("No Tweet Found with id: " + id);
        }

        Tweet originalTweet = originalTweetOptional.get();
        User author = userRepository.findByUsername(tweetRequestDto.getCredentials().getUsername());

        validateUser(author, tweetRequestDto.getCredentials());

        Tweet newReplyTweet = new Tweet();
        newReplyTweet.setContent(tweetRequestDto.getContent());
        newReplyTweet.setAuthor(author);
        newReplyTweet.setInReplyTo(originalTweet);


        newReplyTweet.setHashtags(new ArrayList<>());
        newReplyTweet.setMentionedUsers(new ArrayList<>());
        Tweet savedReplyTweet = tweetRepository.saveAndFlush(newReplyTweet);
        String[] words = newReplyTweet.getContent().split(" ");

        for (String s : words) {
            if (s.substring(0, 1).equals("@") && userRepository.findByUsername(s.substring(1, s.length())) != null) {
                savedReplyTweet.getMentionedUsers().add((userRepository.findByUsername(s.substring(1, s.length()))));
            }

            if (s.substring(0, 1).equals("#")) {
                Hashtag h = hashtagRepository.findByLabel(s.substring(1, s.length()));
                if (h == null) {
                    HashtagDto newHashtag = new HashtagDto();
                    newHashtag.setLabel(s.substring(1, s.length()));
                    savedReplyTweet.getHashtags().add(hashtagRepository.saveAndFlush(hashtagMapper.hashtagDtoToEntity(newHashtag)));
                }
                savedReplyTweet.getHashtags().add(h);
            }
        }
//        Tweet savedReplyTweet = tweetRepository.saveAndFlush(newReplyTweet);
        return tweetMapper.entityToResponseDto(savedReplyTweet);
    }
    public List<TweetResponseDto> getTweetReposts(Long id){
        Optional<Tweet> foundTweet = tweetRepository.findById(id);
        if(foundTweet.isEmpty() || foundTweet.get().isDeleted()){
            throw new NotFoundException("No Tweet found with given ID");
        }

        return tweetMapper.entitiesToResponseDtos(tweetRepository.findByRepostOfAndDeletedFalse(foundTweet.get()));
    }


    @Override
    public List<UserResponseDto> getTweetMentions(Long id){
        Optional<Tweet> foundTweet = tweetRepository.findById(id);
        if (foundTweet.isEmpty() || foundTweet.get().isDeleted()){
            throw new NotFoundException("No tweet found with given ID");
        }

        return userMapper.entitiesToResponseDtos(userRepository.getMentioned(foundTweet.get().getId()));
    }
}
