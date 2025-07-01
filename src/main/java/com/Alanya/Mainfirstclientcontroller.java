package com.Alanya;

import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import javafx.scene.paint.ImagePattern;
import com.Alanya.DAO.MessageDAO;
import com.Alanya.DAO.UserContactDAO;
import com.Alanya.DAO.UserDAO;
import com.Alanya.model.AttachmentInfo;
import com.Alanya.services.AttachmentService;
import com.Alanya.services.ContactService;
import com.Alanya.services.NotificationService;
import com.Alanya.services.PresenceService;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Mainfirstclientcontroller implements Initializable {
	
	private List<ClientDisplayWrapper> preloadedContacts;
	private Map<Integer, Integer> preloadedUnreadCounts;
	
	@FXML private GridPane emojiGridPane;

	@FXML
	public ListView<ClientDisplayWrapper> contactListView;
	@FXML
	private VBox messagesContainerVBox;
	@FXML
	private ScrollPane messagesScrollPane;
	@FXML private Button editProfilePicButton; 
	@FXML
	private TextField messageField;
	@FXML
	private Label contactChatHeaderLabel;
	@FXML
	private Label statusLabel;

	@FXML
	private VBox messageInputContainer;
	@FXML
	private BorderPane centerChatPane;
	@FXML
	private VBox sidebarVBox;

	@FXML
	private Label currentUserLabel;
	@FXML
	private HBox attachmentPreviewPane;
	@FXML
	private Label attachmentFileNameLabel;
	@FXML
	private Button cancelAttachmentButton;
	@FXML
	private ImageView defaultConversationImage;

	// Variables audio et vidéo
	private Stage audioCallStage;
	private AudioCallController audioCallController;
	private Stage videoCallStage;
	private VideoCallController videoCallController;
	
	

	private ContextMenu chatContextMenu;
	
	private String activeCallId = null;
	private String activeCallPartner = null;
	private volatile boolean isInCall = false;
	private String callType = null;

	private ServerSocket p2pAudioServerSocket;
	private ServerSocket p2pVideoServerSocket;
	private Thread p2pAudioThread;
	private Thread p2pVideoThread;

	private Socket p2pAudioSocket;
	private ObjectOutputStream p2pAudioOut;
	private ObjectInputStream p2pAudioIn;
	private volatile boolean audioStreaming = false;

	private TargetDataLine microphone;
	private SourceDataLine speakers;

	private Socket p2pVideoSocket;
	private ObjectOutputStream p2pVideoOut;
	private ObjectInputStream p2pVideoIn;
	private volatile boolean videoStreaming = false;
	private VideoCapture videoCapture;
	private ImageView localVideoView;
	private ImageView remoteVideoView;

	private static final String EMOJI_PHONE_GREEN = "📞";
	private static final String EMOJI_PHONE_RED_BARRED = "📵";
	private static final String EMOJI_CAMERA_GREEN = "📹";
	private static final String EMOJI_CAMERA_RED_BARRED = "🚫📹";

	@FXML
	private HBox contactNAMEHBOX;
	

	// Services
	NotificationService notificationService;
	private ContactService contactService;
	private PresenceService presenceService;
	public UserDAO userDAO;

	
	public Client currentUser;
	private String clientPassword;

	private ClientServer clientP2PServer;
	private Thread clientP2PServerThread;
	private CentralServerConnection centralServerConnection;
	
	// Pour le nouveau bouton "plus" et sa popup
    @FXML private Button moreOptionsButton; // fx:id="moreOptionsButton"
    @FXML private VBox attachmentOptionsPopup; // fx:id="attachmentOptionsPopup"
    @FXML private Button openFileButton; // fx:id="openFileButton"
    @FXML private Button openCameraButton; // fx:id="openCameraButton"
    
    @FXML
    private Button emojiPickerButton;
    
    private static final AudioFormat STANDARD_AUDIO_FORMAT = new AudioFormat(
    	    AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 1, 2, 16000, false
    	);
    private long callStartTimeMillis;
    private Timeline callTimer;
    private Label activeCallTimerLabel;
    
    // Correspondance entre unicode emoji et nom de fichier
    private Map<String, String> emojiToFileMap = new HashMap<>();
    
    // Liste des emojis disponibles
    private List<EmojiData> availableEmojis = new ArrayList<>();
    
    private boolean isEmojiGridVisible = false;
    
    // Taille des emojis en pixels
    private static final int EMOJI_SIZE = 24;
    private static final int EMOJI_BUTTON_SIZE = 32;
    
    

    
    // Pour la capture vidéo
    private Stage cameraPreviewStage;
    private ImageView cameraFrameView; // Pour afficher la prévisualisation de la caméra
    private volatile boolean isCameraActive = false;
    private Mat currentCameraFrame; // Pour stocker la frame OpenCV capturée

    // Pour les statuts de lecture (emojis - la couleur sera gérée par CSS)
    private static final String EMOJI_SENT = " ✓";       // Un seul juste gris
    private static final String EMOJI_DELIVERED = " ✓✓";  // Deux justes gris
    private static final String EMOJI_READ = " ✓✓";       // Deux justes bleus (via classe CSS)


	public final Map<String, PeerSession> activePeerSessions = new ConcurrentHashMap<>();
	public final ObservableList<ClientDisplayWrapper> myPersonalContactsList = FXCollections.observableArrayList();
	final Map<String, Boolean> contactOnlineStatusMap = new HashMap<>();
	private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

	private final String ONLINE_ICON_PATH = "/com/Alanya/userc.png";
	private final String OFFLINE_ICON_PATH = "/com/Alanya/usernc.jpeg";
	private final String DEFAULT_AVATAR_PATH = "/com/Alanya/compte-utilisateur.png";
	private final String DEFAULT_CHAT_BACKGROUND_PATH = "/com/Alanya/Interface_Acceuil_Alanya.png";
	private Image defaultChatBackground;

	AttachmentService attachmentService;
	
	

	private Image onlineIcon;
	private Image offlineIcon;
	private Image defaultAvatar;

	private LocalDate lastMessageDateDisplayed = null;

	private final Map<String, File> localDownloadedFiles = new ConcurrentHashMap<>();
	private final Map<String, DoubleProperty> fileDownloadProgressMap = new ConcurrentHashMap<>();
	private final Map<String, BufferedOutputStream> activeFileDownloadsOutputStreams = new ConcurrentHashMap<>();
	private final Map<String, Long> expectedFileDownloadSizes = new ConcurrentHashMap<>();
	private final Map<String, String> activeDownloadFinalSavePaths = new ConcurrentHashMap<>();
	private final Map<String, BiConsumer<Boolean, File>> activeDownloadCallbacks = new ConcurrentHashMap<>();

	@FXML
	private Button sendMessageButton; 
	@FXML private Button audioCallButton; 
	@FXML private Button videoCallButton; 
	@FXML
	private VBox voiceRecordingPane;
	@FXML
	private Label recordingTimerLabel;
	@FXML
	private HBox voiceVisualizer;
	
    @FXML private Circle currentUserAvatar;
    @FXML private Label currentUsernameLabel;
    private int lastKnownCaretPosition = 0;

	private static final String EMOJI_SEND_TEXT = "➤";
	private static final String EMOJI_MICROPHONE_TEXT = "🎤";
	private static final String EMOJI_STOP_RECORDING_TEXT = "◼️"; 

	private boolean isRecordingVoice = false;
	private ByteArrayOutputStream recordedAudioBytes;
	private AudioFileFormat.Type audioFileType = AudioFileFormat.Type.WAVE;
	private AudioFormat audioFormat;
	private Thread recordingThread;
	private long recordingStartTime;

	private static final String ICON_DOWNLOAD_TEXT = "↓";
	private static final String ICON_OPEN_TEXT = "↗";
	private static final String ICON_ERROR_TEXT = "⚠️";
	private static final String ICON_LOADING_TEXT = "⏳";
	private static final String ICON_FILE_TEXT = "📄";
	
	
	
	
	public static void loadInitialDataInBackground(Client user) {
	    if (user == null || user.getId() <= 0) return;
	    
	    // Simule un chargement de données (contacts, messages non lus, etc.)
	    // Note : cette méthode est statique et sera appelée depuis le thread de connexion.
	    // Elle ne peut pas mettre à jour l'UI directement.
	    Mainfirstclientcontroller tempController = new Mainfirstclientcontroller();
	    tempController.currentUser = user; // Assigne temporairement l'utilisateur
	    tempController.preloadedContacts = tempController.fetchUserContactsFromDB(user.getId());
	    
	    MessageDAO dao = new MessageDAO();
	    try {
	        tempController.preloadedUnreadCounts = dao.getUnreadMessageCountsPerSender(user.getId());
	    } catch(SQLException e) {
	        e.printStackTrace();
	        tempController.preloadedUnreadCounts = new HashMap<>();
	    }
	}

	
	public void setupApplication(Client user, String password, List<ClientDisplayWrapper> contacts, Map<Integer, Integer> unreadCounts) {
	    // 1. Définir l'utilisateur authentifié
	    setAuthenticatedUser(user, password);
	    
	    // 2. Appliquer les données pré-chargées à l'interface
	    if (contacts != null) {
	        myPersonalContactsList.setAll(contacts);
	    }
	    if (unreadCounts != null && notificationService != null) {
	        notificationService.setInitialUnreadCounts(unreadCounts);
	    }
	    
	    // 3. Lancer les connexions réseau en arrière-plan
	    connectToServer();
	    
	    // 4. Rafraîchir l'interface immédiatement avec les données disponibles
	    Platform.runLater(() -> {
	        contactListView.refresh();
	        // Le rafraîchissement des statuts "en ligne" se fera
	        // progressivement à mesure que le serveur répond.
	        refreshContactStatuses();
	    });
	}
	
	public void applyPreloadedData() {
	    Platform.runLater(() -> {
	        if (preloadedContacts != null) {
	            myPersonalContactsList.setAll(preloadedContacts);
	            System.out.println(preloadedContacts.size() + " contacts pré-chargés appliqués à l'UI.");
	        }
	        if (preloadedUnreadCounts != null && notificationService != null) {
	            notificationService.loadInitialUnreadCounts(currentUser.getId());
	            System.out.println("Compteurs de messages non lus appliqués.");
	        }
	        contactListView.refresh();
	        refreshContactStatuses();
	    });
	}

	public static class ClientDisplayWrapper {
        private final Client client;
        private String displayName;

        public ClientDisplayWrapper(Client client, String displayName) {
            this.client = client;
            this.displayName = (displayName == null || displayName.trim().isEmpty()) ? client.getNomUtilisateur() : displayName;
        }

        public Client getClient() { return client; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = (displayName == null || displayName.trim().isEmpty()) ? this.client.getNomUtilisateur() : displayName; }
        @Override public String toString() { return displayName; }
    }

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	    // --- Initialisation des services ---
	    userDAO = new UserDAO();
	    notificationService = new NotificationService(new MessageDAO(), userDAO);
	    contactService = new ContactService(new UserContactDAO(), userDAO);
	    presenceService = new PresenceService(userDAO);
	    attachmentService = new AttachmentService();
	    attachmentService.setMainController(this);
	    contactService.setMainController(this);

	    contactListView.setItems(myPersonalContactsList);
	    setupContactListViewCellFactory();
	    contactListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
	        if (newValue != null) {
	            openDiscussionForContact(newValue.getClient(), newValue.getDisplayName());
	        } else {
	            handleBack(null);
	        }
	    });
	    messageField.setOnKeyPressed(this::handleEnterKeyPressed);

	    if (notificationService != null) {
	        notificationService.setContactListRefreshCallback(senderIdToRefresh -> {
	            Platform.runLater(() -> contactListView.refresh());
	        });
	    }
	    
	    notificationService.setMessageDisplayCallback(this::displayMessage);
	    
	    try {
	        // Chargement de l'icône pour l'appel audio
	        Image audioIcon = new Image(getClass().getResourceAsStream("/com/Alanya/appel.png"));
	        ImageView audioIconView = new ImageView(audioIcon);
	        audioIconView.setFitHeight(24); 
	        audioIconView.setFitWidth(24);
	        audioCallButton.setGraphic(audioIconView); 
	        
	        Image videoIcon = new Image(getClass().getResourceAsStream("/com/Alanya/videocall.png"));
	        ImageView videoIconView = new ImageView(videoIcon);
	        videoIconView.setFitHeight(24); 
	        videoIconView.setFitWidth(24);
	        videoCallButton.setGraphic(videoIconView); 
	    } catch (Exception e) {
	        System.err.println("Erreur lors du chargement des icônes d'appel : " + e.getMessage());
	        // En cas d'erreur (si les fichiers n'existent pas), on met un texte de secours
	        audioCallButton.setText("📞");
	        videoCallButton.setText("📹");
	    }

	    if (messagesScrollPane != null && messagesContainerVBox != null) {
	        messagesScrollPane.setContent(messagesContainerVBox);
	        messagesScrollPane.setFitToWidth(true);
	        messagesContainerVBox.heightProperty().addListener((obs, oldVal, newVal) -> {
	            if (newVal.doubleValue() > oldVal.doubleValue()) {
	                messagesScrollPane.setVvalue(1.0);
	            }
	        });
	    }

	    if (emojiPickerButton != null) {
	        try {
	            Image emojiIcon = new Image(getClass().getResourceAsStream("/com/Alanya/emoji.png"));
	            ImageView emojiIconView = new ImageView(emojiIcon);
	            emojiIconView.setFitHeight(22);
	            emojiIconView.setFitWidth(22);
	            emojiPickerButton.setGraphic(emojiIconView);
	            emojiPickerButton.setText(null);
	            emojiPickerButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
	        } catch (Exception e) {
	            emojiPickerButton.setText("😀");
	        }
	    }

	    initializeEmojiMapping();
	    loadAvailableEmojis();
	    setupEmojiGrid();

	    // Configuration de l'aperçu des pièces jointes
	    attachmentPreviewPane = new HBox(5);
	    attachmentPreviewPane.setAlignment(Pos.CENTER_LEFT);
	    attachmentPreviewPane.setPadding(new Insets(5, 0, 5, 0));
	    attachmentPreviewPane.setVisible(false);
	    attachmentPreviewPane.setManaged(false);
	    attachmentFileNameLabel = new Label();
	    cancelAttachmentButton = new Button("X");
	    cancelAttachmentButton.setOnAction(this::handleCancelAttachment);
	    HBox.setHgrow(attachmentFileNameLabel, Priority.ALWAYS);
	    attachmentPreviewPane.getChildren().addAll(new Label("Fichier: "), attachmentFileNameLabel, cancelAttachmentButton);

	    if (messageInputContainer != null) {
	        messageInputContainer.getChildren().add(0, attachmentPreviewPane);
	    }

	    // Configuration de l'image d'accueil par défaut
	    defaultConversationImage = new ImageView();
	    try {
	        defaultChatBackground = new Image(getClass().getResourceAsStream(DEFAULT_CHAT_BACKGROUND_PATH));
	        defaultConversationImage.setImage(defaultChatBackground);
	        defaultConversationImage.setPreserveRatio(true);
	        defaultConversationImage.setFitHeight(500);
	        StackPane.setAlignment(defaultConversationImage, Pos.CENTER);
	        Node messagesNode = messagesScrollPane;
	        StackPane centerStack = new StackPane(defaultConversationImage, messagesNode);
	        centerChatPane.setCenter(centerStack);
	    } catch (Exception e) {
	        System.err.println("Impossible de charger l'image de fond par défaut: " + e.getMessage());
	    }
	    
	    // Visibilité initiale des éléments
	    handleBack(null);

	    // Charger les icônes et l'avatar par défaut
	    onlineIcon = loadImage(ONLINE_ICON_PATH);
	    offlineIcon = loadImage(OFFLINE_ICON_PATH);
	    defaultAvatar = loadImage(DEFAULT_AVATAR_PATH);

	    // Configuration des boutons et popups
	    if (attachmentOptionsPopup != null) {
	        attachmentOptionsPopup.setVisible(false);
	        attachmentOptionsPopup.setManaged(false);
	    }
	    if (messageField != null && sendMessageButton != null) {
	        messageField.textProperty().addListener((observable, oldValue, newValue) -> {
	            updateSendButtonState(newValue, attachmentService != null && attachmentService.getCurrentAttachmentInfo() != null);
	        });
	        updateSendButtonState(messageField.getText(), attachmentService != null && attachmentService.getCurrentAttachmentInfo() != null);
	    }
	    if (voiceRecordingPane != null) {
			voiceRecordingPane.setVisible(false);
			voiceRecordingPane.setManaged(false);
			if (voiceVisualizer == null) {
				voiceVisualizer = new HBox(2);
				voiceVisualizer.setAlignment(Pos.CENTER_LEFT); 
				voiceVisualizer.setMinHeight(25); 
			}
		}
	    
	    // Configuration du menu contextuel du chat
	    chatContextMenu = new ContextMenu();
	    MenuItem displayContactItem = new MenuItem("Afficher contact");
	    displayContactItem.setOnAction(e -> {
	        ClientDisplayWrapper selectedWrapper = contactListView.getSelectionModel().getSelectedItem();
	        if (selectedWrapper != null) {
	            Client contact = selectedWrapper.getClient();
	            String displayName = selectedWrapper.getDisplayName();
	            StringBuilder details = new StringBuilder();
	            details.append("Nom d'affichage : ").append(displayName).append("\n");
	            details.append("Nom d'utilisateur : ").append(contact.getNomUtilisateur()).append("\n");
	            details.append("Téléphone : ").append(contact.getTelephone() != null ? contact.getTelephone() : "Non renseigné").append("\n");
	            details.append("Email : ").append(contact.getEmail() != null ? contact.getEmail() : "Non renseigné");
	            showAlert(AlertType.INFORMATION, "Détails du Contact", details.toString());
	        } else {
	            showAlert(AlertType.WARNING, "Aucun Contact", "Veuillez sélectionner une discussion pour voir les détails du contact.");
	        }
	    });
	    chatContextMenu.getItems().add(displayContactItem);

	    updateStatus("Interface initialisée. En attente de connexion au serveur central.");
	}
	
	public void processIncomingP2PMessage(Message message) {
	    if (message == null || currentUser == null) return;

	    try {
	        // On a besoin de l'ID de l'expéditeur pour la logique de notification
	        int senderId = userDAO.getUserIdByUsername(message.getSender());
	        if (senderId == -1) return; // Expéditeur inconnu

	        // On récupère l'ID du contact actuellement affiché (peut être null)
	        ClientDisplayWrapper selectedWrapper = contactListView.getSelectionModel().getSelectedItem();
	        Integer contactCurrentlyBeingViewedId = (selectedWrapper != null) ? selectedWrapper.getClient().getId() : null;
	        
	        // On passe toutes les informations au service de notification
	        notificationService.onMessageReceived(
	            message,
	            currentUser.getId(),
	            contactCurrentlyBeingViewedId,
	            senderId
	        );

	    } catch (SQLException e) {
	        System.err.println("Erreur lors du traitement du message P2P entrant : " + e.getMessage());
	        e.printStackTrace();
	    }
	}


	private void setupContactListViewCellFactory() {
        contactListView.setCellFactory(param -> {
            ListCell<ClientDisplayWrapper> cell = new ListCell<>() {
                private final HBox hbox = new HBox(10);
                private final Circle contactAvatar = new Circle(15);
                private final VBox nameAndPresenceVBox = new VBox(0);
                private final Label nameLabel = new Label();
                private final Label presenceDetailsLabel = new Label();
                private final Pane spacer = new Pane();
                private final Label unreadCountLabel = new Label();
                private final ContextMenu contactMenu = new ContextMenu();
                

                {
                	 MenuItem renameItem = new MenuItem("Modifier le nom");
                	 renameItem.setStyle("-fx-text-fill: black;"); 
                     renameItem.setOnAction(event -> {
                         ClientDisplayWrapper wrapper = getItem();
                         if (wrapper != null && currentUser != null && contactService != null) {
                             // Utilise la méthode du service pour ouvrir la boîte de dialogue
                             contactService.editContactNameDialog(currentUser.getId(), wrapper);
                         }
                     });

                     MenuItem deleteItem = new MenuItem("Supprimer le contact");
                     deleteItem.setStyle("-fx-text-fill: black;");
                     deleteItem.setOnAction(event -> {
                         ClientDisplayWrapper wrapper = getItem();
                         if (wrapper != null && currentUser != null && contactService != null) {
                             // Utilise la méthode du service pour gérer la suppression
                             contactService.deleteContact(currentUser.getId(), wrapper);
                         }
                     });

                     // Ajout des options au menu
                     contactMenu.getItems().addAll(renameItem, new SeparatorMenuItem(), deleteItem);
                     
                     // On attache le menu contextuel à la cellule
                     setContextMenu(contactMenu);
                    presenceDetailsLabel.setStyle("-fx-font-size: 0.8em; -fx-text-fill: #A0A0A0;");
                    unreadCountLabel.getStyleClass().add("unread-count-label");
                    unreadCountLabel.setStyle("-fx-background-color: #0078D7;" + "-fx-text-fill: white;"
							+ "-fx-padding: 2px 6px;" + "-fx-background-radius: 10;" + "-fx-font-size: 0.85em;"
							+ "-fx-font-weight: bold;");
                    nameAndPresenceVBox.getChildren().addAll(nameLabel, presenceDetailsLabel);
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    hbox.getChildren().addAll(contactAvatar, nameAndPresenceVBox, spacer, unreadCountLabel);
                    setPadding(new Insets(3, 0, 3, 0));
                }

                @Override
                protected void updateItem(ClientDisplayWrapper wrapper, boolean empty) {
                    super.updateItem(wrapper, empty);
                    if (empty || wrapper == null || wrapper.getClient() == null) {
                        setGraphic(null);
                    } else {
                        Client contactClient = wrapper.getClient();
                        nameLabel.setText(wrapper.getDisplayName());

                        byte[] profilePicBytes = contactClient.getProfilePicture();
                        Image avatarImg = null;

                        System.out.println("[DEBUG UI] Affichage contact: " + contactClient.getNomUtilisateur() 
                                         + " | Données photo disponibles: " + (profilePicBytes != null && profilePicBytes.length > 0));

                        if (profilePicBytes != null && profilePicBytes.length > 0) {
                            avatarImg = bytesToImage(profilePicBytes);
                        }
                        
                        if (avatarImg != null) {
                            // On utilise l'image du contact
                            contactAvatar.setFill(new ImagePattern(avatarImg));
                        } else {
                            // On utilise l'avatar par défaut si aucune image n'est trouvée
                            contactAvatar.setFill(new ImagePattern(defaultAvatar));
                        }
                        
                        // Le reste de votre logique pour le statut...
                        boolean isOnline = contactOnlineStatusMap.getOrDefault(contactClient.getNomUtilisateur(), false);
                        if (isOnline) {
                            presenceDetailsLabel.setText("En ligne");
                            presenceDetailsLabel.setStyle("-fx-font-size: 0.8em; -fx-text-fill: #2ECC71;");
                        } else {
                            String lastSeen = presenceService.getPresenceStatusForContact(contactClient.getId(), false);
                            presenceDetailsLabel.setText(lastSeen);
                            presenceDetailsLabel.setStyle("-fx-font-size: 0.8em; -fx-text-fill: #A0A0A0;");
                        }

                        int unreadCount = notificationService.getUnreadCount(contactClient.getId());
                        if (unreadCount > 0) {
                            unreadCountLabel.setText(String.valueOf(unreadCount));
                            unreadCountLabel.setVisible(true);
                        } else {
                            unreadCountLabel.setVisible(false);
                        }
                        setGraphic(hbox);
                    }
                }
            };
            return cell;
        });
    }
	
	private void populateEmojiGrid() {
        if (emojiGridPane == null) return;

        // Liste d'emojis que vous pouvez personnaliser
        List<String> emojis = List.of(
            "😀", "😂", "😍", "🤔", "😭", "🙏", "👍", "❤️",
            "😊", "😎", "😢", "😠", "😮", "�", "🔥", "💯"
        );

        int col = 0;
        int row = 0;
        int maxCols = 8; // Nombre d'emojis par ligne

        for (String emoji : emojis) {
            Button emojiButton = new Button(emoji);
            emojiButton.getStyleClass().add("emoji-button");
            emojiButton.setOnAction(e -> insertEmoji(emoji));
            
            emojiGridPane.add(emojiButton, col, row);
            
            col++;
            if (col >= maxCols) {
                col = 0;
                row++;
            }
        }
    }
    
	 private void insertEmoji(String emoji) {
	        if (messageField != null) {
	            messageField.appendText(emoji);
	        }
	    }
    
    
    
    
	 @FXML
	    void handleEditProfilePic(ActionEvent event) {
	        if (currentUser == null) {
	            showAlert(AlertType.ERROR, "Erreur", "Utilisateur non connecté.");
	            return;
	        }
	        try {
	            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/Alanya/ProfilePictureEditor.fxml"));
	            Parent root = loader.load();

	            ProfilePictureEditorController editorController = loader.getController();
	            editorController.initData(this, currentUser);

	            Stage stage = new Stage();
	            stage.setTitle("Éditeur de Photo de Profil");
	            stage.setScene(new Scene(root));
	            stage.initModality(Modality.APPLICATION_MODAL);
	            stage.initOwner(this.getStage()); // 'this.getStage()' est une méthode que vous avez déjà
	            stage.setResizable(false);
	            stage.showAndWait();

	        } catch (IOException e) {
	            e.printStackTrace();
	            showAlert(AlertType.ERROR, "Erreur d'interface", "Impossible de charger la fenêtre d'édition.");
	        }
	    }
	 
	 public void updateProfilePictureUI(Image image) {
	        Platform.runLater(() -> {
	            if (currentUserAvatar != null) {
	                Image imageToShow = image;
	                if (imageToShow == null) {
	                    try {
	                        imageToShow = new Image(getClass().getResourceAsStream("/com/Alanya/compte-utilisateur.png"));
	                    } catch (Exception e) {
	                        currentUserAvatar.setFill(Color.GREY);
	                        return;
	                    }
	                }
	                // On applique le recadrage ici aussi pour la cohérence
	                currentUserAvatar.setFill(new ImagePattern(cropToSquare(imageToShow)));
	            }
	        });
	    }
	    
	 
	 private Image cropToSquare(Image image) {
	        if (image == null) return null;
	        
	        double width = image.getWidth();
	        double height = image.getHeight();
	        double size = Math.min(width, height);

	        double xOffset = (width - size) / 2;
	        double yOffset = (height - size) / 2;

	        ImageView imageView = new ImageView(image);
	        Rectangle2D viewport = new Rectangle2D(xOffset, yOffset, size, size);
	        imageView.setViewport(viewport);

	        SnapshotParameters params = new SnapshotParameters();
	        params.setFill(Color.TRANSPARENT);
	        
	        WritableImage croppedImage = new WritableImage((int) size, (int) size);
	        imageView.snapshot(params, croppedImage);
	        
	        return croppedImage;
	    }
	 
	 
    @FXML
    void handleEditBackground(ActionEvent event) {
        showAlert(AlertType.INFORMATION, "Fonctionnalité en cours", "La modification de l'arrière-plan de la discussion sera bientôt disponible.");
    }

    @FXML
    void handleTheme(ActionEvent event) {
        showAlert(AlertType.INFORMATION, "Fonctionnalité en cours", "Le changement de thème (Sombre/Clair) sera bientôt disponible.");
    }
    
    private static class EmojiData {
        String unicode;
        String fileName;
        String description;
        
        EmojiData(String unicode, String fileName, String description) {
            this.unicode = unicode;
            this.fileName = fileName;
            this.description = description;
        }
    }
    private void initializeEmojiMapping() {
        emojiToFileMap.put("😀", "1f600.png");     // grinning face
        emojiToFileMap.put("😃", "1f603.png");     // grinning face with big eyes
        emojiToFileMap.put("😄", "1f604.png");     // grinning face with smiling eyes
        emojiToFileMap.put("😁", "1f601.png");     // beaming face with smiling eyes
        emojiToFileMap.put("😆", "1f606.png");     // grinning squinting face
        emojiToFileMap.put("😅", "1f605.png");     // grinning face with sweat
        emojiToFileMap.put("😂", "1f602.png");     // face with tears of joy
        emojiToFileMap.put("🤣", "1f923.png");     // rolling on the floor laughing
        emojiToFileMap.put("😊", "1f60a.png");     // smiling face with smiling eyes
        emojiToFileMap.put("😇", "1f607.png");     // smiling face with halo
        
        emojiToFileMap.put("🙂", "1f642.png");     // slightly smiling face
        emojiToFileMap.put("🙃", "1f643.png");     // upside-down face
        emojiToFileMap.put("😉", "1f609.png");     // winking face
        emojiToFileMap.put("😌", "1f60c.png");     // relieved face
        emojiToFileMap.put("😍", "1f60d.png");     // smiling face with heart-eyes
        emojiToFileMap.put("🥰", "1f970.png");     // smiling face with hearts
        emojiToFileMap.put("😘", "1f618.png");     // face blowing a kiss
        emojiToFileMap.put("😗", "1f617.png");     // kissing face
        emojiToFileMap.put("😙", "1f619.png");     // kissing face with smiling eyes
        emojiToFileMap.put("😚", "1f61a.png");     // kissing face with closed eyes
        
        emojiToFileMap.put("😋", "1f60b.png");     // face savoring food
        emojiToFileMap.put("😛", "1f61b.png");     // face with tongue
        emojiToFileMap.put("😝", "1f61d.png");     // squinting face with tongue
        emojiToFileMap.put("😜", "1f61c.png");     // winking face with tongue
        emojiToFileMap.put("🤪", "1f929.png");     // zany face
        emojiToFileMap.put("🤨", "1f928.png");     // face with raised eyebrow
        emojiToFileMap.put("🧐", "1f9d0.png");     // face with monocle
        emojiToFileMap.put("🤓", "1f913.png");     // nerd face
        emojiToFileMap.put("😎", "1f60e.png");     // smiling face with sunglasses
        emojiToFileMap.put("🤩", "1f929.png");     // star-struck
        
        // Emojis de coeur
        emojiToFileMap.put("❤️", "2764.png");      // red heart
        emojiToFileMap.put("🧡", "1f9e1.png");     // orange heart
        emojiToFileMap.put("💛", "1f49b.png");     // yellow heart
        emojiToFileMap.put("💚", "1f49a.png");     // green heart
        emojiToFileMap.put("💙", "1f499.png");     // blue heart
        emojiToFileMap.put("💜", "1f49c.png");     // purple heart
        emojiToFileMap.put("🖤", "1f5a4.png");     // black heart
        emojiToFileMap.put("🤍", "1f90d.png");     // white heart
        emojiToFileMap.put("🤎", "1f90e.png");     // brown heart
        emojiToFileMap.put("💔", "1f494.png");     // broken heart
        
        // Emojis de main
        emojiToFileMap.put("👍", "1f44d.png");     // thumbs up
        emojiToFileMap.put("👎", "1f44e.png");     // thumbs down
        emojiToFileMap.put("👌", "1f44c.png");     // OK hand
        emojiToFileMap.put("✌️", "270c.png");      // victory hand
        emojiToFileMap.put("🤞", "1f91e.png");     // crossed fingers
        emojiToFileMap.put("🤟", "1f91f.png");     // love-you gesture
        emojiToFileMap.put("🤘", "1f918.png");     // sign of the horns
        emojiToFileMap.put("🤙", "1f919.png");     // call me hand
        emojiToFileMap.put("👋", "1f44b.png");     // waving hand
        emojiToFileMap.put("👏", "1f44f.png");     // clapping hands
    }
 // Méthode pour charger les emojis disponibles
    private void loadAvailableEmojis() {
        availableEmojis.clear();
        for (Map.Entry<String, String> entry : emojiToFileMap.entrySet()) {
            availableEmojis.add(new EmojiData(entry.getKey(), entry.getValue(), "")); // Description peut être ajoutée
        }
    }
    
    private String getEmojiDescription(String unicode) {
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("😀", "Visage souriant");
        descriptions.put("😍", "Visage avec des yeux en coeur");
        descriptions.put("😂", "Visage avec des larmes de joie");
        descriptions.put("👍", "Pouce en l'air");
        descriptions.put("❤️", "Coeur rouge");
        descriptions.put("🎉", "Confettis");
        // Ajoutez plus de descriptions selon vos besoins
        
        return descriptions.getOrDefault(unicode, "Emoji");
    }

    // --- CORRECTION ---
    // Méthode pour peupler la grille d'emojis avec des images
    private void setupEmojiGrid() {
        if (emojiGridPane == null) return;
        emojiGridPane.getChildren().clear();
        
        // Chemin standard vers les ressources emoji
        final String EMOJI_RESOURCE_PATH = "/com/Alanya/assets/emojis/";
        
        int columns = 8;
        int row = 0;
        int col = 0;

        for (EmojiData emojiData : availableEmojis) {
            try {
                // Construit le chemin complet de la ressource
                String fullPath = EMOJI_RESOURCE_PATH + emojiData.fileName;
                InputStream imageStream = getClass().getResourceAsStream(fullPath);

                // Vérifie si la ressource a été trouvée
                if (imageStream == null) {
                    System.err.println("Impossible de charger l'image emoji (ressource non trouvée): " + fullPath);
                    continue; // Passe à l'emoji suivant
                }

                Image emojiImage = new Image(imageStream);
                ImageView imageView = new ImageView(emojiImage);
                imageView.setFitWidth(24);
                imageView.setFitHeight(24);

                Button button = new Button();
                button.setGraphic(imageView);
                button.getStyleClass().add("emoji-button");
                button.setOnAction(e -> handleEmojiClick(emojiData.unicode));
                
                emojiGridPane.add(button, col, row);
                col++;
                if (col >= columns) {
                    col = 0;
                    row++;
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la création du bouton pour l'emoji : " + emojiData.fileName);
                e.printStackTrace();
            }
        }
    }

    private Button createEmojiImageButton(EmojiData emojiData) {
        try {
            // Charger l'image depuis les ressources
            InputStream imageStream = getClass().getResourceAsStream("/emojis/png/" + emojiData.fileName);
            if (imageStream == null) {
                System.err.println("Image non trouvée: " + emojiData.fileName);
                return null;
            }

            Image emojiImage = new Image(imageStream, EMOJI_SIZE, EMOJI_SIZE, true, true);
            ImageView imageView = new ImageView(emojiImage);
            
            Button button = new Button();
            button.setGraphic(imageView);
            button.setPrefSize(EMOJI_BUTTON_SIZE, EMOJI_BUTTON_SIZE);
            button.setMinSize(EMOJI_BUTTON_SIZE, EMOJI_BUTTON_SIZE);
            button.setMaxSize(EMOJI_BUTTON_SIZE, EMOJI_BUTTON_SIZE);
            
            // Style du bouton
            button.setStyle("-fx-background-color: transparent; " +
                           "-fx-border-color: transparent; " +
                           "-fx-cursor: hand; " +
                           "-fx-background-radius: 4px; " +
                           "-fx-padding: 2px;");
            
            // Tooltip avec description
            Tooltip tooltip = new Tooltip(emojiData.description);
            button.setTooltip(tooltip);
            
            // Effets de survol
            button.setOnMouseEntered(e -> {
                button.setStyle("-fx-background-color: #f0f0f0; " +
                               "-fx-border-color: #cccccc; " +
                               "-fx-cursor: hand; " +
                               "-fx-background-radius: 4px; " +
                               "-fx-border-radius: 4px; " +
                               "-fx-padding: 2px;");
                // Agrandir légèrement l'image
                imageView.setFitWidth(EMOJI_SIZE + 2);
                imageView.setFitHeight(EMOJI_SIZE + 2);
            });
            
            button.setOnMouseExited(e -> {
                button.setStyle("-fx-background-color: transparent; " +
                               "-fx-border-color: transparent; " +
                               "-fx-cursor: hand; " +
                               "-fx-background-radius: 4px; " +
                               "-fx-padding: 2px;");
                // Remettre la taille normale
                imageView.setFitWidth(EMOJI_SIZE);
                imageView.setFitHeight(EMOJI_SIZE);
            });

            // Action lors du clic
            button.setOnAction(e -> handleEmojiClick(emojiData.unicode));

            imageStream.close();
            return button;
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'emoji " + emojiData.fileName + ": " + e.getMessage());
            return null;
        }
    }

    @FXML
    void handleSupport(ActionEvent event) {
        showAlert(AlertType.INFORMATION, "Service Client", "Pour toute assistance, veuillez contacter le support à l'adresse : support@alanya.com.");
    }

    @FXML
    void handleEmoji(ActionEvent event) {
        // Inverse simplement l'état de visibilité actuel
        isEmojiGridVisible = !isEmojiGridVisible; 
        emojiGridPane.setVisible(isEmojiGridVisible);
        emojiGridPane.setManaged(isEmojiGridVisible); 
    }
	public void updateSendButtonStateBasedOnAttachment() {
        if (messageField != null) { // S'assurer que messageField n'est pas null
            updateSendButtonState(messageField.getText(), (attachmentService != null && attachmentService.getCurrentAttachmentInfo() != null));
        } else {
            // Si messageField est null, on suppose qu'il n'y a pas de texte et on vérifie seulement la pièce jointe
            updateSendButtonState("", (attachmentService != null && attachmentService.getCurrentAttachmentInfo() != null));
        }
    }
	
	private void toggleEmojiGrid() {
        isEmojiGridVisible = !isEmojiGridVisible;
        emojiGridPane.setVisible(isEmojiGridVisible);
        emojiGridPane.setManaged(isEmojiGridVisible);
        
        statusLabel.setText(isEmojiGridVisible ? 
            "Sélectionnez un emoji coloré" : "Prêt");
    }

	// Méthode appelée lors du clic sur un emoji
	private void handleEmojiClick(String emojiUnicode) {
	    if (messageField != null) {
	        // On n'utilise plus requestFocus() ni getCaretPosition()
	        // On utilise notre variable fiable `lastKnownCaretPosition`
	        messageField.insertText(lastKnownCaretPosition, emojiUnicode);
	    }
	}
	
	// Méthode pour créer le contenu du message (gère le texte et les emojis)
		private Node createMessageContentNode(String content) {
		    TextFlow textFlow = new TextFlow();
		    final String EMOJI_RESOURCE_PATH = "/com/Alanya/assets/emojis/";

		    String emojiRegex = emojiToFileMap.keySet().stream()
		        .map(Pattern::quote)
		        .collect(Collectors.joining("|"));

		    Pattern emojiPattern = Pattern.compile(emojiRegex);
		    Matcher matcher = emojiPattern.matcher(content);

		    int lastEnd = 0;
		    while (matcher.find()) {
		        if (matcher.start() > lastEnd) {
		            textFlow.getChildren().add(new Text(content.substring(lastEnd, matcher.start())));
		        }
		        String emojiUnicode = matcher.group();
		        String emojiFileName = emojiToFileMap.get(emojiUnicode);
		        try {
		            String fullPath = EMOJI_RESOURCE_PATH + emojiFileName;
		            InputStream imageStream = getClass().getResourceAsStream(fullPath);

		            if (imageStream == null) {
		                System.err.println("Emoji non trouvé pour affichage dans message: " + fullPath);
		                textFlow.getChildren().add(new Text(emojiUnicode)); // Fallback: affiche l'emoji texte
		                continue;
		            }
		            
		            Image emojiImage = new Image(imageStream);
		            ImageView emojiView = new ImageView(emojiImage);
		            emojiView.setFitHeight(20);
		            emojiView.setFitWidth(20);
		            textFlow.getChildren().add(emojiView);
		        } catch (Exception e) {
		            System.err.println("Erreur affichage emoji dans message: " + emojiFileName);
		            textFlow.getChildren().add(new Text(emojiUnicode)); // Fallback en cas d'erreur
		        }
		        lastEnd = matcher.end();
		    }

		    if (lastEnd < content.length()) {
		        textFlow.getChildren().add(new Text(content.substring(lastEnd)));
		    }
		    return textFlow;
		}


    private void updateSendButtonState(String text, boolean attachmentIsPending) {
        if (sendMessageButton == null) {
            System.err.println("updateSendButtonState: sendMessageButton est null.");
            return;
        }

        // Effacer le contenu précédent (texte ou graphique)
        sendMessageButton.setText(null);
        sendMessageButton.setGraphic(null);

        if (isRecordingVoice) {
            // Mode: Arrêter l'enregistrement
            sendMessageButton.setText(EMOJI_STOP_RECORDING_TEXT); // Garde le carré pour l'arrêt
            sendMessageButton.setOnAction(this::handleStopRecordingAndSendVoice);
            sendMessageButton.getStyleClass().setAll("btn-send-recording");
        } else if (attachmentIsPending || (text != null && !text.trim().isEmpty())) {
        	 try {
                 Image sendImage = new Image(getClass().getResourceAsStream("/com/Alanya/envoi.png"));
                 ImageView sendImageView = new ImageView(sendImage);
                 sendImageView.setFitWidth(20);
                 sendImageView.setFitHeight(20);
                 sendMessageButton.setGraphic(sendImageView);
             } catch (Exception e) {
                 System.err.println("Erreur chargement image envoi.png: " + e.getMessage());
                 sendMessageButton.setText("➤"); // Texte de secours
             }
            sendMessageButton.setOnAction(this::handleSendMessageButton);
            sendMessageButton.getStyleClass().setAll("btn-send");
        } else {
            // NOUVELLE LOGIQUE POUR LE MICROPHONE
            // Mode: Démarrer l'enregistrement vocal
            try {
                // Charge l'image du microphone depuis les ressources
                Image micImage = new Image(getClass().getResourceAsStream("/com/Alanya/microphone.png"));
                ImageView micImageView = new ImageView(micImage);
                micImageView.setFitWidth(20);  // Ajustez la taille de l'icône
                micImageView.setFitHeight(20); // Ajustez la taille de l'icône
                
                sendMessageButton.setGraphic(micImageView); // Utilise setGraphic au lieu de setText
                
            } catch (Exception e) {
                // Fallback en cas d'erreur de chargement de l'image
                System.err.println("Erreur chargement image microphone: " + e.getMessage());
                sendMessageButton.setText("🎤"); // Texte de secours
            }
            
            sendMessageButton.setOnAction(this::handleStartRecordingVoice);
            sendMessageButton.getStyleClass().setAll("btn-microphone");
        }
    }
	
	
	public void selectAndOpenDiscussionForUser(Client userToSelect) {
		if (userToSelect == null)
			return;
		Platform.runLater(() -> {
			for (ClientDisplayWrapper wrapper : contactListView.getItems()) {
				if (wrapper.getClient().getId() == userToSelect.getId()) {
					contactListView.getSelectionModel().select(wrapper);
					break;
				}
			}
		});
	}

	private void openEditContactNameDialog(ClientDisplayWrapper contactWrapper) {
		if (contactService != null && currentUser != null) {
			contactService.editContactNameDialog(currentUser.getId(), contactWrapper);
		} else {
			showAlert(AlertType.ERROR, "Erreur", "Service de contact non initialisé.");
		}
	}

	private void deleteUserContact(ClientDisplayWrapper contactWrapper) {
		if (contactService != null && currentUser != null) {
			contactService.deleteContact(currentUser.getId(), contactWrapper);
		} else {
			showAlert(AlertType.ERROR, "Erreur", "Service de contact non initialisé.");
		}
	}

	public HostServices getHostServicesFromApplication() {
		if (App.getInstance() != null) {
			return App.getInstance().getHostServices();
		}
		if (contactListView != null && contactListView.getScene() != null
				&& contactListView.getScene().getWindow() instanceof Stage) {
			Stage mainStage = (Stage) contactListView.getScene().getWindow();
		}
		return null;
	}

	private Image loadImage(String path) {
		try {
			URL resourceUrl = getClass().getResource(path);
			if (resourceUrl == null) {
				System.err.println("Resource not found: " + path + ". Using placeholder.");
				return new Image(getClass().getResource("/com/Alanya/icons/placeholder_icon.png").toExternalForm());
			}
			return new Image(resourceUrl.toExternalForm());
		} catch (Exception e) {
			System.err.println("Impossible de charger l'image : " + path + " - " + e.getMessage());
			try {
				return new Image(getClass().getResource("/com/Alanya/icons/placeholder_icon.png").toExternalForm());
			} catch (Exception ex) {
				System.err.println("Impossible de charger l'image placeholder ! " + ex.getMessage());
				return null;
			}
		}
	}

	


	public void handleMessageStatusUpdate(MessageStatusUpdateMessage update, String fromPeer) {
        Platform.runLater(() -> {
            try {
                // 1. Mettre à jour la base de données
                new MessageDAO().updateMessageReadStatus(update.getMessageDatabaseId(), update.getNewStatus());

                // 2. Mettre à jour l'interface si la discussion est ouverte
                ClientDisplayWrapper selectedContactWrapper = contactListView.getSelectionModel().getSelectedItem();
                if (selectedContactWrapper != null && selectedContactWrapper.getClient().getNomUtilisateur().equals(fromPeer)) {
                    for (Node rowNode : messagesContainerVBox.getChildren()) {
                        if (rowNode.getUserData() instanceof Message) {
                            Message msg = (Message) rowNode.getUserData();
                            if (msg.getDatabaseId() == update.getMessageDatabaseId()) {
                                msg.setReadStatus(update.getNewStatus()); // Mettre à jour l'objet en mémoire

                                // Parcourir la bulle pour trouver le label de statut
                                if (rowNode instanceof HBox) {
                                    HBox messageRowHBox = (HBox) rowNode;
                                    VBox messageBubbleVBox = (VBox) messageRowHBox.getChildren().get(0);
                                    HBox timeAndStatusBox = (HBox) messageBubbleVBox.getChildren().get(messageBubbleVBox.getChildren().size() - 1);
                                    if (timeAndStatusBox.getChildren().size() > 2) { // S'assure qu'il y a un indicateur
                                        Label statusIndicatorLabel = (Label) timeAndStatusBox.getChildren().get(2);

                                        if (update.getNewStatus() == 2) { // Lu
                                            statusIndicatorLabel.setText(" ✓✓");
                                            statusIndicatorLabel.getStyleClass().setAll("message-status-indicator", "read");
                                        } else if (update.getNewStatus() == 1) { // Reçu
                                            statusIndicatorLabel.setText(" ✓✓");
                                            statusIndicatorLabel.getStyleClass().setAll("message-status-indicator", "delivered");
                                        }
                                    }
                                }
                                break; // Message trouvé et mis à jour, on arrête de chercher
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
	public void openDiscussionForContact(Client contact, String displayName) {
        if (contact == null) {
            handleBack(null);
            return;
        }
        Platform.runLater(() -> {
            updateChatHeaderWithPresence(contact, displayName);

            if (messagesContainerVBox != null)
                messagesContainerVBox.getChildren().clear();

            List<Message> messageHistory = new ArrayList<>();
            if (currentUser != null && currentUser.getId() > 0) {
                // Charger l'historique
                loadMessageHistory(currentUser.getId(), contact.getId());
                
                // Marquer les messages comme lus dans le service de notification
                if (notificationService != null) {
                    notificationService.onDiscussionOpened(contact.getId(), currentUser.getId());
                }

                // === NOUVELLE LOGIQUE : ENVOYER LES ACCUSÉS DE LECTURE "LU" ===
                try {
                    MessageDAO dao = new MessageDAO();
                    messageHistory = dao.getMessageHistory(currentUser.getId(), contact.getId()); // Obtenir l'historique à jour
                    PeerSession sessionWithContact = activePeerSessions.get(contact.getNomUtilisateur());
                    if (sessionWithContact != null && sessionWithContact.isConnected()) {
                        for (Message msg : messageHistory) {
                            // Si le message a été reçu par moi et n'est pas encore "lu", envoyer la notif
                            if (msg.getReceiver().equals(currentUser.getNomUtilisateur()) && msg.getReadStatus() < 2) {
                                MessageStatusUpdateMessage statusUpdate = new MessageStatusUpdateMessage(msg.getDatabaseId(), 2); // Statut 2 = Lu
                                sessionWithContact.sendP2PObject(statusUpdate);
                            }
                        }
                        System.out.println("Accusés de lecture envoyés à: " + contact.getNomUtilisateur());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            } else {
                System.err.println(
                        "openDiscussionForContact: currentUser ou son ID est invalide. Impossible de charger l'historique ou gérer les notifications.");
            }

            if (defaultConversationImage != null) {
                defaultConversationImage.setVisible(false);
                defaultConversationImage.setManaged(false);
            }
            if (messagesScrollPane != null) {
                messagesScrollPane.setVisible(true);
                messagesScrollPane.setManaged(true);
            }
            if (messageInputContainer != null) {
                messageInputContainer.setVisible(true);
                messageInputContainer.setManaged(true);
            }
            if (contactNAMEHBOX != null) {
                contactNAMEHBOX.setVisible(true);
                contactNAMEHBOX.setManaged(true);
            }

            if (activePeerSessions != null && (!activePeerSessions.containsKey(contact.getNomUtilisateur())
                    || (activePeerSessions.get(contact.getNomUtilisateur()) != null
                    && !activePeerSessions.get(contact.getNomUtilisateur()).isConnected()))) {
                if (contact.getLastKnownIp() != null && !contact.getLastKnownIp().isEmpty()
                        && contact.getLastKnownPort() > 0) {
                    connectToPeer(contact.getNomUtilisateur(), contact.getLastKnownIp(), contact.getLastKnownPort());
                } else {
                    if (centralServerConnection != null && centralServerConnection.isConnected()) {
                        centralServerConnection.requestPeerInfo(contact.getNomUtilisateur());
                    } else {
                        showAlert(AlertType.WARNING, "Connexion P2P",
                                "Non connecté au serveur central pour obtenir les infos de " + displayName);
                    }
                }
            }
        });
    }
	private void refreshContactStatuses() {
		if (currentUser == null || currentUser.getId() <= 0) {
			return;
		}
		if (centralServerConnection != null && centralServerConnection.isConnected()) {
			System.out.println("Rafraîchissement des statuts des contacts via serveur central...");
			Platform.runLater(() -> {
				for (ClientDisplayWrapper wrapper : myPersonalContactsList) {
					if (wrapper != null && wrapper.getClient() != null) {
						centralServerConnection.requestPeerInfo(wrapper.getClient().getNomUtilisateur());
					}
				}
			});
		} else {
			System.out.println("Non connecté au serveur central, tentative de mise à jour des statuts depuis la BDD.");
			Platform.runLater(() -> {
				for (ClientDisplayWrapper wrapper : myPersonalContactsList) {
					if (wrapper != null && wrapper.getClient() != null) {
						ClientDisplayWrapper selectedContactWrapper = contactListView.getSelectionModel()
								.getSelectedItem();
						if (selectedContactWrapper != null
								&& selectedContactWrapper.getClient().getId() == wrapper.getClient().getId()) {
							updateChatHeaderWithPresence(wrapper.getClient(), wrapper.getDisplayName());
						}
					}
				}
				contactListView.refresh();
			});
		}
	}

	public void loadMyPersonalContacts() {
		if (this.currentUser == null || this.currentUser.getId() <= 0) {
			System.err.println("Utilisateur actuel non défini ou ID invalide. Impossible de charger les contacts.");
			myPersonalContactsList.clear();
			return;
		}
		List<ClientDisplayWrapper> contactsWrappers = fetchUserContactsFromDB(this.currentUser.getId());
		Platform.runLater(() -> {
			myPersonalContactsList.setAll(contactsWrappers);
			contactListView.refresh();
			System.out.println(
					contactsWrappers.size() + " contacts personnels chargés pour " + currentUser.getNomUtilisateur());
			refreshContactStatuses();
		});
	}

	public static List<ClientDisplayWrapper> fetchUserContactsFromDB(int ownerUserId) {
	    List<ClientDisplayWrapper> contacts = new ArrayList<>();
	    String sql = "SELECT u.id, u.nom_utilisateur, u.email, u.telephone, u.statut, u.est_admin, u.last_known_ip, u.last_known_port, u.profile_picture, uc.nom_personnalise "
	            + "FROM UserContacts uc JOIN Utilisateurs u ON uc.contact_user_id = u.id "
	            + "WHERE uc.owner_user_id = ?";
	    
	    try (Connection conn = DatabaseConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, ownerUserId);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                Client contactUser = new Client(
	                    rs.getInt("u.id"), rs.getString("u.nom_utilisateur"), rs.getString("u.email"),
	                    rs.getString("u.telephone"), rs.getString("u.statut"), rs.getBoolean("u.est_admin"),
	                    rs.getString("u.last_known_ip"), rs.getInt("u.last_known_port")
	                );
	                byte[] profilePicBytes = rs.getBytes("u.profile_picture");
	                if (profilePicBytes != null) {
	                    contactUser.setProfilePicture(profilePicBytes);
	                }
	                String nomPersonnalise = rs.getString("nom_personnalise");
	                contacts.add(new ClientDisplayWrapper(contactUser, nomPersonnalise));
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        // Dans un contexte statique, on ne peut pas montrer d'alerte. On log l'erreur.
	        System.err.println("Impossible de charger les contacts personnels pour l'utilisateur ID: " + ownerUserId);
	    }
	    return contacts;
	}

	private void connectToPeer(String peerUsername, String host, int port) {
		if (currentUser == null || currentUser.getNomUtilisateur().equals(peerUsername)) {
			System.out.println("Tentative de connexion P2P à soi-même (" + peerUsername + ") annulée.");
			return;
		}
		PeerSession existingSession = activePeerSessions.get(peerUsername);
		if (existingSession != null && existingSession.isConnected()) {
			System.out.println("Déjà connecté en P2P à " + peerUsername);
			updateStatus("Déjà connecté à " + peerUsername);
			updatePeerInfo(peerUsername, host, port, true);
			return;
		}
		if (existingSession != null) {
			existingSession.close();
			activePeerSessions.remove(peerUsername);
		}

		updateStatus("Tentative de connexion P2P à " + peerUsername + " sur " + host + ":" + port);
		new Thread(() -> {
			try {
				Socket peerSocket = new Socket(host, port);
				ObjectOutputStream outStream = new ObjectOutputStream(peerSocket.getOutputStream());
				outStream.flush();
				ObjectInputStream inStream = new ObjectInputStream(peerSocket.getInputStream());

				outStream.writeObject(new AuthMessage(currentUser.getNomUtilisateur()));
				outStream.flush();

				PeerSession session = new PeerSession(peerUsername, peerSocket, inStream, outStream, this);
				activePeerSessions.put(peerUsername, session);
				new Thread(session).start();

				Platform.runLater(() -> {
					updateStatus("Connecté à " + peerUsername);
					updatePeerInfo(peerUsername, host, port, true);
				});

			} catch (IOException e) {
				System.err.println("Erreur de connexion P2P à " + peerUsername + " sur " + host + ":" + port + " - "
						+ e.getMessage());
				Platform.runLater(() -> {
					showAlert(AlertType.ERROR, "Erreur Connexion P2P",
							"Impossible de se connecter à " + peerUsername + ".");
					updateStatus("Échec connexion à " + peerUsername);
					updatePeerInfo(peerUsername, null, 0, false);
				});
			}
		}).start();
	}

	public void connectToServer() {
		if (currentUser == null || currentUser.getNomUtilisateur() == null || clientPassword == null) {
			showAlert(AlertType.ERROR, "Erreur Connexion",
					"Informations utilisateur incomplètes pour la connexion au serveur central.");
			return;
		}
		updateStatus("Connexion au serveur central en cours...");
		centralServerConnection = new CentralServerConnection(this);
		if (centralServerConnection.connect("localhost", 9000)) {
			updateStatus("Connecté au serveur central. Authentification...");
			Map<String, String> authData = new HashMap<>();
			authData.put("identifier", currentUser.getNomUtilisateur());
			authData.put("password", clientPassword);
			centralServerConnection
					.sendCommand(new ServerCommand(ServerCommand.ServerCommandType.AUTHENTICATE, authData));
		} else {
			updateStatus("Échec de connexion socket au serveur central.");
			showAlert(AlertType.ERROR, "Erreur Connexion",
					"Impossible de se connecter au serveur central. Vérifiez qu'il est démarré et accessible.");
		}
	}

	 public void handleReceivedFileChunk(FileChunkMessage chunkMsg, String fromPeerUsername) {
	        Platform.runLater(() -> {
	            String fileName = chunkMsg.getFileName();
	            long messageDbId = findMessageDbIdForFile(fileName, fromPeerUsername);
	            if (messageDbId == -1) {
	                System.err.println("MFC: Impossible de lier le chunk au message pour " + fileName);
	                return;
	            }
	            String uniqueFileKey = messageDbId + "!" + fileName;

	            String finalUserPath = activeDownloadFinalSavePaths.get(uniqueFileKey);
	            if (finalUserPath == null) {
	                System.err.println("MFC: Aucun chemin de sauvegarde trouvé pour la clé " + uniqueFileKey);
	                cleanupFailedDownload(uniqueFileKey, null);
	                return;
	            }
	            Path tempFilePath = Paths.get(new File(finalUserPath).getParent(), fileName + ".part_" + System.currentTimeMillis());

	            try {
	                BufferedOutputStream bos = activeFileDownloadsOutputStreams.get(uniqueFileKey);
	                // Si c'est le premier morceau, on crée le flux
	                if (bos == null) {
	                    Files.createDirectories(tempFilePath.getParent());
	                    bos = new BufferedOutputStream(new FileOutputStream(tempFilePath.toFile()));
	                    activeFileDownloadsOutputStreams.put(uniqueFileKey, bos);
	                    updateFileProgress(uniqueFileKey, 0.001); // Indique le début
	                }

	                // On écrit les données
	                if (chunkMsg.getChunkData() != null && chunkMsg.getChunkData().length > 0) {
	                    bos.write(chunkMsg.getChunkData());
	                }

	                // Si c'est le dernier morceau, on finalise
	                if (chunkMsg.isLastChunk()) {
	                    bos.close(); // Fermer le flux est essentiel
	                    activeFileDownloadsOutputStreams.remove(uniqueFileKey);

	                    long expectedSize = expectedFileDownloadSizes.getOrDefault(uniqueFileKey, -1L);
	                    long actualSize = Files.size(tempFilePath);

	                    if (expectedSize != -1 && actualSize != expectedSize) {
	                        throw new IOException("Taille de fichier incorrecte. Attendu: " + expectedSize + ", Reçu: " + actualSize);
	                    }

	                    Path finalPathTarget = Paths.get(finalUserPath);
	                    Files.move(tempFilePath, finalPathTarget, StandardCopyOption.REPLACE_EXISTING);
	                    System.out.println("MFC: Fichier téléchargé et sauvegardé dans : " + finalPathTarget);

	                    // Mise à jour de l'état post-téléchargement
	                    File finalFile = finalPathTarget.toFile();
	                    localDownloadedFiles.put(messageDbId + "_" + fileName, finalFile); // Cache mémoire
	                    attachmentService.saveDownloadedFilePathPref(messageDbId, fileName, finalFile.getAbsolutePath()); // Persistance

	                    updateFileProgress(uniqueFileKey, 1.0); // Fin de la progression
	                    showAlert(AlertType.INFORMATION, "Téléchargement Terminé", "Fichier '" + fileName + "' sauvegardé.");
	                    
	                    BiConsumer<Boolean, File> cb = activeDownloadCallbacks.get(uniqueFileKey);
	                    if (cb != null) cb.accept(true, finalFile);
	                    
	                    // Rafraîchir l'UI pour que le bouton devienne "Ouvrir"
	                    contactListView.refresh();

	                } else {
	                    // Mettre à jour la barre de progression
	                    long totalReceived = tempFilePath.toFile().length();
	                    long expectedSize = expectedFileDownloadSizes.getOrDefault(uniqueFileKey, -1L);
	                    if (expectedSize > 0) {
	                        updateFileProgress(uniqueFileKey, (double) totalReceived / expectedSize);
	                    }
	                }
	            } catch (IOException e) {
	                System.err.println("MFC: Erreur de téléchargement pour " + fileName + ": " + e.getMessage());
	                showAlert(AlertType.ERROR, "Erreur Téléchargement", "Erreur lors de la sauvegarde du fichier : " + e.getMessage());
	                cleanupFailedDownload(uniqueFileKey, tempFilePath);
	                contactListView.refresh(); // Rafraîchir pour montrer le bouton en erreur
	            } finally {
	                if (chunkMsg.isLastChunk()) {
	                    // Nettoyage final des maps de suivi
	                    expectedFileDownloadSizes.remove(uniqueFileKey);
	                    activeDownloadFinalSavePaths.remove(uniqueFileKey);
	                    activeDownloadCallbacks.remove(uniqueFileKey);
	                }
	            }
	        });
	    }

	private void cleanupFailedDownload(String uniqueFileKey, Path tempFilePath) {
		try {
			BufferedOutputStream bos = activeFileDownloadsOutputStreams.remove(uniqueFileKey);
			if (bos != null)
				bos.close();
			if (tempFilePath != null && Files.exists(tempFilePath)) { // Vérifier si tempFilePath est non null
				Files.deleteIfExists(tempFilePath);
			}
		} catch (IOException e) {
			System.err.println(
					"MFC: Erreur nettoyage téléchargement échoué pour " + uniqueFileKey + ": " + e.getMessage());
		}
		expectedFileDownloadSizes.remove(uniqueFileKey);
		activeDownloadFinalSavePaths.remove(uniqueFileKey);
		// Le callback est géré par l'appelant de cleanupFailedDownload ou dans le bloc
		// catch principal
		updateFileProgress(uniqueFileKey, -1);
	}

	private void updateFileProgress(String uniqueFileKey, double progressValue) {
		Platform.runLater(() -> {
			DoubleProperty progressProp = fileDownloadProgressMap.get(uniqueFileKey);

			if (progressProp == null) {
				if (progressValue > 0.0 && progressValue < 1.0) { // Uniquement créer si c'est une progression
																	// intermédiaire valide
					// System.out.println("MFC: ProgressProperty pour clé '" + uniqueFileKey + "'
					// non trouvée, création pour progression " + progressValue);
					progressProp = fileDownloadProgressMap.computeIfAbsent(uniqueFileKey,
							k -> new SimpleDoubleProperty(0.0));
					progressProp.set(progressValue);
				} else {
					// System.out.println("MFC: ProgressProperty pour clé '" + uniqueFileKey + "'
					// est null pour progression finale/erreur " + progressValue + ". Pas de
					// création/mise à jour.");
				}
			} else {
				progressProp.set(progressValue);
			}

			if (progressValue >= 1.0 || progressValue < 0.0) {
				fileDownloadProgressMap.remove(uniqueFileKey);
				// Nettoyage des autres maps est maintenant géré dans le bloc 'isLastChunk' ou
				// 'cleanupFailedDownload'
				// pour s'assurer que le callback est appelé avant de tout supprimer.
				// System.out.println("MFC: ProgressProperty retirée pour clé: " + uniqueFileKey
				// + " due à progression: " + progressValue);
			}
		});
	}

	private long findMessageDbIdForFile(String fileName, String senderUsername) {
		for (Node rowNode : messagesContainerVBox.getChildrenUnmodifiable()) {
			if (rowNode.getUserData() instanceof Message) {
				Message msg = (Message) rowNode.getUserData();
				if (msg.getAttachmentInfo() != null && msg.getAttachmentInfo().getFileName().equals(fileName)
						&& msg.getSender().equals(senderUsername)) {
					return msg.getDatabaseId();
				}
			}
		}
		// System.err.println("MFC: Impossible de trouver l'ID de message pour le
		// fichier " + fileName + " de " + senderUsername + " (pourrait être un nouveau
		// message non encore totalement affiché).");
		return -1; // Retourner -1 si non trouvé pour l'instant
	}

	private void updateAttachmentButtonInChat(String fileName, String localPath, long messageDbId) {
		for (Node rowNode : messagesContainerVBox.getChildren()) {
			if (rowNode.getUserData() instanceof Message) {
				Message msg = (Message) rowNode.getUserData();
				if (msg.getDatabaseId() == messageDbId && msg.getAttachmentInfo() != null
						&& msg.getAttachmentInfo().getFileName().equals(fileName)) {
					if (rowNode instanceof HBox) {
						HBox messageRowHBox = (HBox) rowNode;
						for (Node childOfRow : messageRowHBox.getChildren()) {
							if (childOfRow instanceof VBox) {
								VBox messageBubbleVBox = (VBox) childOfRow;
								for (Node bubbleChild : messageBubbleVBox.getChildren()) {
									if (bubbleChild.getStyleClass().contains("attachment-box")) {
										VBox attachmentDisplayVBox = (VBox) bubbleChild;
										if (attachmentDisplayVBox.getChildren().size() > 1
												&& attachmentDisplayVBox.getChildren().get(1) instanceof Button) {
											Button actionButton = (Button) attachmentDisplayVBox.getChildren().get(1);
											msg.getAttachmentInfo().setLocalPath(localPath);
											configureOpenButton(actionButton, localPath);
											if (attachmentDisplayVBox.getChildren().size() > 2 && attachmentDisplayVBox
													.getChildren().get(2) instanceof ProgressIndicator) {
												attachmentDisplayVBox.getChildren().get(2).setVisible(false);
											}
											System.out.println("MFC: Bouton mis à jour en 'Ouvrir' pour: " + fileName
													+ " à " + localPath);
											return;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		System.err.println(
				"MFC: Impossible de trouver le bouton de pièce jointe dans l'UI pour mettre à jour: " + fileName);
	}

	class PeerSession implements Runnable {
		private final String peerUsername;
		private final Socket socket;
		private final ObjectInputStream in;
		private final ObjectOutputStream out;
		private final Mainfirstclientcontroller uiController;
		private volatile boolean connected = true;

		public PeerSession(String peerUsername, Socket socket, ObjectInputStream in, ObjectOutputStream out,
				Mainfirstclientcontroller controller) {
			this.peerUsername = peerUsername;
			this.socket = socket;
			this.in = in;
			this.out = out;
			this.uiController = controller;
			System.out.println("P2P Session créée pour " + peerUsername + " avec " + socket.getInetAddress());
		}

		 @Override
	        public void run() {
	            try {
	                while (connected && socket != null && !socket.isClosed() && in != null) {
	                	Object receivedObj = in.readObject();


	                    if (receivedObj instanceof MessageStatusUpdateMessage) { 
	                        uiController.handleMessageStatusUpdate((MessageStatusUpdateMessage) receivedObj, peerUsername);
	                    } 
	                    else if (receivedObj instanceof AudioPacket) {
	                         // Logique pour les paquets audio (inchangée)
	                    } 
	                    else if (receivedObj instanceof VideoPacket) {
	                         // Logique pour les paquets vidéo (inchangée)
	                    }

	                }
	            } catch (Exception e) {
	                // ...
	            } finally {
	                close();
	            }
	    }
		public void sendP2PObject(Serializable object) {
			if (connected && out != null && socket != null && !socket.isClosed()) {
				try {
					synchronized (out) {
						out.writeObject(object);
						out.flush();
						out.reset();
					}
					System.out.println(
							"PeerSession: Objet " + object.getClass().getSimpleName() + " envoyé à " + peerUsername);
				} catch (IOException e) {
					System.err.println("Erreur d'envoi P2P à " + peerUsername + " pour objet "
							+ object.getClass().getSimpleName() + ": " + e.getMessage());
					close();
				}
			} else {
				System.err.println(
						"Envoi P2P impossible à " + peerUsername + ": session non connectée ou flux invalide.");
			}
		}

		public void sendMessage(Message message) {
			sendP2PObject(message);
		}

		public boolean isConnected() {
			return connected && socket != null && !socket.isClosed();
		}

		public void close() {
			if (!connected && socket == null)
				return;
			connected = false;
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				/* ignorer */ }
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				/* ignorer */ }
			try {
				if (socket != null && !socket.isClosed())
					socket.close();
			} catch (IOException e) {
				/* ignorer */ }

			if (peerUsername != null) {
				uiController.activePeerSessions.remove(peerUsername, this);
				Platform.runLater(() -> uiController.updatePeerInfo(peerUsername, null, 0, false));
			}
			System.out.println("Session P2P avec " + (peerUsername != null ? peerUsername : "inconnu") + " fermée.");
		}

		public void sendFileInChunks(File fileToSend, String requestedFileNameByPeer) {
			if (!isConnected() || fileToSend == null || !fileToSend.exists()) {
				System.err.println(
						"PeerSession: Impossible d'envoyer le fichier. Session non connectée ou fichier invalide: "
								+ (fileToSend != null ? fileToSend.getAbsolutePath() : "null"));
				sendP2PObject(new FileTransferStatusMessage(requestedFileNameByPeer, "ERROR_FILE_NOT_FOUND"));
				return;
			}

			new Thread(() -> {
				try (BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(fileToSend))) {
					byte[] buffer = new byte[8192];
					int bytesRead;
					int chunkIndex = 0;
					long fileSize = fileToSend.length();
					long totalSent = 0;

					System.out.println("PeerSession: Début envoi fichier " + fileToSend.getName() + " (" + fileSize
							+ " bytes) à " + peerUsername);

					while ((bytesRead = fileInputStream.read(buffer)) != -1) {
						if (!isConnected()) {
							System.err.println(
									"PeerSession: Envoi fichier annulé, session P2P avec " + peerUsername + " fermée.");
							return;
						}
						totalSent += bytesRead;
						boolean isLast = (totalSent == fileSize);
						byte[] chunkData = new byte[bytesRead];
						System.arraycopy(buffer, 0, chunkData, 0, bytesRead);

						FileChunkMessage chunk = new FileChunkMessage(requestedFileNameByPeer, chunkData, chunkIndex++,
								isLast);
						sendP2PObject(chunk);
						// System.out.println("PeerSession: Envoyé morceau " + chunkIndex + "
						// ("+bytesRead+" bytes) pour " + requestedFileNameByPeer + ". Dernier: " +
						// isLast); // Peut être verbeux
					}
					if (fileSize == 0) { // Gérer le cas d'un fichier vide
						FileChunkMessage emptyLastChunk = new FileChunkMessage(requestedFileNameByPeer, new byte[0], 0,
								true);
						sendP2PObject(emptyLastChunk);
						System.out.println("PeerSession: Envoyé dernier morceau (vide) pour fichier vide "
								+ requestedFileNameByPeer);
					}

					System.out.println(
							"PeerSession: Envoi complet du fichier " + fileToSend.getName() + " à " + peerUsername);
				} catch (IOException e) {
					System.err.println("PeerSession: Erreur IOException lors de l'envoi du fichier "
							+ fileToSend.getName() + " à " + peerUsername + ": " + e.getMessage());
					sendP2PObject(new FileTransferStatusMessage(requestedFileNameByPeer, "ERROR_SENDING_FILE"));
				}
			}, "P2P-FileSendThread-" + requestedFileNameByPeer).start();
		}
	}

	private VBox createAttachmentDisplayNode(Message originalMessage, AttachmentInfo attachment, boolean isOutgoing) {
        VBox attachmentBox = new VBox(5);
        attachmentBox.getStyleClass().add("attachment-box");

        Text fileIconText = new Text("📄");
        fileIconText.setStyle("-fx-font-size: 18px;");
        Label fileNameLabel = new Label(attachment.getFileName() + " (" + formatFileSize(attachment.getFileSize()) + ")");
        HBox fileInfoLine = new HBox(5, fileIconText, fileNameLabel);

        Button fileActionButton = new Button();
        fileActionButton.getStyleClass().add("file-action-button");
        
        File localFile = null;
        if (isOutgoing) {
            // Pour l'expéditeur, le chemin est celui du fichier original
            localFile = new File(attachment.getLocalPath());
        } else {
            // Pour le récepteur, on utilise notre nouvelle méthode de recherche simple et fiable
            localFile = findDownloadedFile(originalMessage.getDatabaseId(), attachment.getFileName(), attachment.getFileSize());
        }

        if (localFile != null && localFile.exists()) {
            // Si le fichier est trouvé, le bouton est "Ouvrir"
            configureOpenButton(fileActionButton, localFile.getAbsolutePath());
        } else {
            // Ce cas ne devrait plus se produire pour un récepteur après réception.
            // S'il se produit, cela indique une erreur de sauvegarde.
            fileActionButton.setText("Erreur Fichier");
            fileActionButton.setDisable(true);
            fileActionButton.getStyleClass().add("file-action-error-button");
        }
        
        attachmentBox.getChildren().addAll(fileInfoLine, fileActionButton);
        return attachmentBox;
    }



	 private File findDownloadedFile(long messageDbId, String originalFileName, long expectedSize) {
	        // Nouvelle logique : on construit le nom de fichier attendu et on vérifie s'il existe.
	        String expectedLocalFileName = messageDbId + "-" + originalFileName;
	        File localFile = new File(ClientServer.SAVE_DIRECTORY, expectedLocalFileName);

	        if (localFile.exists() && localFile.length() == expectedSize) {
	            // Le fichier existe et a la bonne taille, c'est notre fichier !
	            return localFile;
	        }

	        return null; // Le fichier n'a pas été trouvé localement.
	    }

	private void configureDownloadButton(Button btn, Message message, AttachmentInfo attachment) {
		btn.setGraphic(new Text(ICON_DOWNLOAD_TEXT));
		btn.setText("");
		btn.getStyleClass().removeAll("open-button", "file-action-error-button");
		btn.getStyleClass().add("download-button");
		btn.setDisable(false);

		btn.setOnAction(e -> {
			btn.setDisable(true);
			btn.setGraphic(new Text(ICON_LOADING_TEXT));

			String progressKey = message.getDatabaseId() + "!" + attachment.getFileName(); // Clé simplifiée
			DoubleProperty currentProgressProp = fileDownloadProgressMap.computeIfAbsent(progressKey,
					k -> new SimpleDoubleProperty(0.0));
			currentProgressProp.set(0.001);

			Node parentNode = btn.getParent();
			if (parentNode != null) {
				ProgressIndicator pi = null;
				if (parentNode instanceof VBox) {
					for (Node nodeInAttachmentBox : ((VBox) parentNode).getChildren()) {
						if (nodeInAttachmentBox instanceof ProgressIndicator) {
							pi = (ProgressIndicator) nodeInAttachmentBox;
							break;
						}
					}
				}

				if (pi != null) {
					pi.progressProperty().unbind();
					pi.progressProperty().bind(currentProgressProp);
					pi.visibleProperty()
							.bind(Bindings.createBooleanBinding(
									() -> currentProgressProp.get() > 0.0 && currentProgressProp.get() < 1.0
											&& Math.abs(currentProgressProp.get() - (-1.0)) > 0.001,
									currentProgressProp));
				}
			}

			if (attachmentService == null) {
				System.err.println("AttachmentService non initialisé dans configureDownloadButton !");
				configureErrorButton(btn, message, attachment);
				return;
			}

			attachmentService.startFileDownload(message, getStage(), // Passer message entier
					() -> {
						Platform.runLater(() -> {
							File downloadedFile = new File(attachment.getLocalPath());
							localDownloadedFiles.put(message.getDatabaseId() + "_" + attachment.getFileName(),
									downloadedFile);
							configureOpenButton(btn, downloadedFile.getAbsolutePath());
						});
					}, () -> {
						Platform.runLater(() -> {
							configureErrorButton(btn, message, attachment);
						});
					});
		});
	}

	private void configureErrorButton(Button btn, Message message, AttachmentInfo attachment) {
		btn.setGraphic(new Text(ICON_ERROR_TEXT));
		btn.setText("Échec");
		btn.getStyleClass().removeAll("open-button", "download-button");
		btn.getStyleClass().add("file-action-error-button");
		btn.setDisable(false);
		btn.setOnAction(e -> configureDownloadButton(btn, message, attachment));
	}

	private void configureOpenButton(Button btn, String filePath) {
		btn.setGraphic(new Text(ICON_OPEN_TEXT));
		btn.setText("");
		btn.getStyleClass().removeAll("download-button", "file-action-error-button");
		btn.getStyleClass().add("open-button");
		btn.setDisable(false);
		btn.setOnAction(e -> {
			if (attachmentService == null) {
				System.err.println("AttachmentService non initialisé dans configureOpenButton !");
				return;
			}
			attachmentService.openAttachment(filePath);
		});
	}

	private String formatFileSize(long size) {
		if (size <= 0)
			return "0 B";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		if (digitGroups >= units.length)
			digitGroups = units.length - 1;
		return new java.text.DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " "
				+ units[digitGroups];
	}

	public Stage getStage() {
		if (contactListView != null && contactListView.getScene() != null) {
			return (Stage) contactListView.getScene().getWindow();
		}
		if (javafx.stage.Stage.getWindows().size() > 0 && javafx.stage.Stage.getWindows().get(0) instanceof Stage) {
			return (Stage) javafx.stage.Stage.getWindows().get(0);
		}
		System.err.println("Impossible de déterminer la Stage actuelle.");
		return null;
	}

	 private void storeSentMessageInDB(Message message, int senderId, int receiverId) {
	        if (senderId <= 0 || receiverId <= 0) { return; }
	        MessageDAO dao = new MessageDAO();
	        try {
	            // La méthode saveMessage met à jour l'ID sur l'objet message directement
	            dao.saveMessage(message, senderId, receiverId);
	        } catch (SQLException e) {
	            e.printStackTrace();
	            showAlert(AlertType.ERROR, "Erreur Base de Données", "Impossible de sauvegarder le message envoyé.");
	        }
	    }

	
	// Remplacer cette méthode dans Mainfirstclientcontroller.java

	 private void loadMessageHistory(int userId1, int userId2) {
	     if (messagesContainerVBox == null) return;
	     
	     messagesContainerVBox.getChildren().clear();
	     lastMessageDateDisplayed = null;

	     MessageDAO dao = new MessageDAO();
	     List<Message> history = new ArrayList<>();
	     try {
	         // La requête SQL trie déjà les messages du plus ancien au plus récent (ORDER BY ... ASC)
	         history = dao.getMessageHistory(userId1, userId2);
	     } catch (SQLException e) {
	         e.printStackTrace();
	         showAlert(AlertType.ERROR, "Erreur Historique", "Impossible de charger l'historique des messages : " + e.getMessage());
	     }

	     // On affiche chaque message. La méthode displayMessage les ajoute bien à la fin de la VBox.
	     for (Message msg : history) {
	         boolean isOutgoing = currentUser != null && msg.getSender().equals(currentUser.getNomUtilisateur());
	         displayMessage(msg, isOutgoing);
	     }

	     // CORRECTION PRINCIPALE : Forcer le défilement vers le bas APRÈS que l'UI a eu le temps de se mettre à jour.
	     Platform.runLater(() -> {
	         if (messagesScrollPane != null) {
	             messagesScrollPane.setVvalue(1.0);
	         }
	     });
	 }

	private Client findClientInSystemById(int userId) {
		if (userId <= 0)
			return null;
		if (currentUser != null && currentUser.getId() == userId)
			return currentUser;
		ClientDisplayWrapper selectedWrapper = contactListView.getSelectionModel().getSelectedItem();
		if (selectedWrapper != null && selectedWrapper.getClient() != null
				&& selectedWrapper.getClient().getId() == userId)
			return selectedWrapper.getClient();
		for (ClientDisplayWrapper wrapper : myPersonalContactsList) {
			if (wrapper != null && wrapper.getClient() != null && wrapper.getClient().getId() == userId)
				return wrapper.getClient();
		}
		return findClientInDBById(userId);
	}

	private Client findClientInDBById(int userId) {
	    if (userDAO == null) {
	        System.err.println("UserDAO non initialisé.");
	        return null;
	    }
	    try {
	        return userDAO.findUserById(userId);
	    } catch (SQLException e) {
	        System.err.println("Erreur lors de la recherche du client par ID " + userId + ": " + e.getMessage());
	        return null;
	    }
	}

	public void displayMessage(Message message, boolean isOutgoing) {
		
		
	    Platform.runLater(() -> {
	        if (messagesContainerVBox == null) return;

	        // Étape 1 : Vérifier si le message appartient à la discussion actuelle
	        ClientDisplayWrapper selectedContactWrapper = contactListView.getSelectionModel().getSelectedItem();
	        boolean messageIsForCurrentChat = false;
	        if (selectedContactWrapper != null && selectedContactWrapper.getClient() != null && currentUser != null) {
	            Client selectedClient = selectedContactWrapper.getClient();
	            if ((message.getSender().equals(currentUser.getNomUtilisateur()) && message.getReceiver().equals(selectedClient.getNomUtilisateur())) ||
	                (message.getSender().equals(selectedClient.getNomUtilisateur()) && message.getReceiver().equals(currentUser.getNomUtilisateur()))) {
	                messageIsForCurrentChat = true;
	            }
	        }
	        if (!messageIsForCurrentChat) return;

	        // Étape 2 : Gérer le séparateur de date
	        try {
	            LocalDateTime messageTimestamp = LocalDateTime.parse(message.getTimestamp(), Message.formatter);
	            LocalDate messageDate = messageTimestamp.toLocalDate();
	            if (lastMessageDateDisplayed == null || !lastMessageDateDisplayed.equals(messageDate)) {
	                HBox dateSeparatorRow = new HBox();
	                dateSeparatorRow.setAlignment(Pos.CENTER);
	                dateSeparatorRow.setPadding(new Insets(10, 0, 10, 0));
	                Label dateLabelSeparator = new Label(formatDateSeparatorText(messageDate));
	                dateLabelSeparator.setStyle("-fx-background-color: #CFD8DC; -fx-text-fill: #37474F; -fx-padding: 3px 10px; -fx-background-radius: 10px;");
	                dateSeparatorRow.getChildren().add(dateLabelSeparator);
	                messagesContainerVBox.getChildren().add(dateSeparatorRow);
	                lastMessageDateDisplayed = messageDate;
	            }
	        } catch (Exception e) {
	            System.err.println("Erreur parsing date: " + message.getTimestamp() + " - " + e.getMessage());
	        }

	        HBox messageRow = new HBox();
	        messageRow.setUserData(message);
	        VBox messageBubbleContent = new VBox(3);

	        if (isOutgoing) {
	            messageBubbleContent.getStyleClass().add("message-bubble-outgoing");
	            messageRow.setAlignment(Pos.CENTER_RIGHT);
	        } else {
	            messageBubbleContent.getStyleClass().add("message-bubble-incoming");
	            messageRow.setAlignment(Pos.CENTER_LEFT);
	        }
	        
	        String content = message.getContent();
	        boolean hasTextContent = content != null && !content.trim().isEmpty();
	        AttachmentInfo attachment = message.getAttachmentInfo();
	        String fileType = (attachment != null && attachment.getFileType() != null) ? attachment.getFileType() : "";
	        boolean isVoiceAttachment = fileType.startsWith("audio/");
	        boolean isImageAttachment = fileType.startsWith("image/");

	        if (isImageAttachment) {
	            messageBubbleContent.getStyleClass().add("media-message-bubble");
	        }
	        if (isVoiceAttachment) {
	            messageBubbleContent.getStyleClass().add("voice-message-bubble");
	        }

	        if (hasTextContent) {
	            if (content.startsWith("Appel") || content.startsWith("APPEL TERMINÉ")) {
	                Label specialMessageLabel = new Label(content);
	                specialMessageLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #555;");
	                messageBubbleContent.getChildren().add(specialMessageLabel);
	            } else {
	                Node contentNode = createMessageContentNode(content);
	                messageBubbleContent.getChildren().add(contentNode);
	            }
	        }

	        if (attachment != null) {
	            Node attachmentNode = null;
	            if (isVoiceAttachment) {
	                attachmentNode = createVoiceMessageDisplayNode(message, attachment, isOutgoing);
	            } else if (isImageAttachment) {
	                attachmentNode = createImageDisplayNode(message, attachment, isOutgoing);
	            } else {
	                attachmentNode = createAttachmentDisplayNode(message, attachment, isOutgoing);
	            }
	            if (attachmentNode != null) {
	                if (hasTextContent) attachmentNode.setTranslateY(5);
	                messageBubbleContent.getChildren().add(attachmentNode);
	            }
	        }

	        HBox timeAndStatusBox = new HBox(5);
	        timeAndStatusBox.setAlignment(Pos.CENTER_RIGHT);
	        Pane spacer = new Pane();
	        HBox.setHgrow(spacer, Priority.ALWAYS);
	        String timeStr = message.getTimestamp();
	        try {
	            timeStr = LocalDateTime.parse(timeStr, Message.formatter).format(DateTimeFormatter.ofPattern("HH:mm"));
	        } catch (Exception e) { /* ignore */ }
	        Label timeLabel = new Label(timeStr);
	        timeLabel.getStyleClass().add("time-label");

	        if (isOutgoing) {
	            Label statusIndicatorLabel = new Label();
	            int messageReadStatus = message.getReadStatus(); // Récupère le statut (0, 1, ou 2)

	            if (messageReadStatus == 2) { // Lu
	                statusIndicatorLabel.setText(EMOJI_READ);
	                statusIndicatorLabel.getStyleClass().setAll("message-status-indicator", "read");
	            } else if (messageReadStatus == 1) { // Reçu
	                statusIndicatorLabel.setText(EMOJI_DELIVERED);
	                statusIndicatorLabel.getStyleClass().setAll("message-status-indicator", "delivered");
	            } else { // Envoyé
	                statusIndicatorLabel.setText(EMOJI_SENT);
	                statusIndicatorLabel.getStyleClass().setAll("message-status-indicator", "sent");
	            }
	            timeAndStatusBox.getChildren().addAll(spacer, timeLabel, statusIndicatorLabel);

	        } else {
	            timeAndStatusBox.getChildren().addAll(spacer, timeLabel);
	        }
	        
	        messageBubbleContent.getChildren().add(timeAndStatusBox);
	        messageRow.getChildren().add(messageBubbleContent);
	        
	        if (!messageBubbleContent.getChildren().isEmpty()) {
	            messagesContainerVBox.getChildren().add(messageRow);
	        }
	        
	        messagesScrollPane.setVvalue(1.0);
	    });
	}
	private String formatDateSeparatorText(LocalDate messageDate) {
		LocalDate today = LocalDate.now();
		LocalDate yesterday = today.minusDays(1);

		if (messageDate.equals(today)) {
			return "Aujourd'hui";
		} else if (messageDate.equals(yesterday)) {
			return "Hier";
		} else {
			// S'assurer que le format est DD/MM/YYYY
			return messageDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		}
	}

	@FXML
    private void handleMoreOptionsButton(ActionEvent event) {
        if (attachmentOptionsPopup != null) {
            boolean estabaVisible = attachmentOptionsPopup.isVisible();
            attachmentOptionsPopup.setVisible(!estabaVisible);
            attachmentOptionsPopup.setManaged(!estabaVisible);
        }
    }
	 
	@FXML
    private void handleOpenFileButton(ActionEvent event) {
        if (attachmentOptionsPopup != null) {
            attachmentOptionsPopup.setVisible(false);
            attachmentOptionsPopup.setManaged(false);
        }
        // Réutilise ta logique existante de sélection de fichier, qui devrait appeler
        // attachmentService.selectFile() et ensuite mettre à jour l'aperçu et le bouton d'envoi
        handleAttachment(event); // Assure-toi que cette méthode appelle displayAttachmentPreview
                                 // et/ou updateSendButtonState indirectement via AttachmentService
    }

	 
	@FXML
    private void handleOpenCamera(ActionEvent event) {
        if (attachmentOptionsPopup != null) {
            attachmentOptionsPopup.setVisible(false);
            attachmentOptionsPopup.setManaged(false);
        }
        // La logique d'ouverture de caméra que je t'ai fournie dans la réponse précédente
        // via openCameraPreview() et captureImageFromPreview() peut être appelée ici.
        // Si tu préfères une vue FXML dédiée pour la caméra :
        openCameraCaptureWindow();
    }
	 
	private void openCameraCaptureWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/Alanya/CameraCaptureView.fxml")); // CHEMIN VERS TON NOUVEAU FXML
            Parent root = loader.load();
            CameraCaptureController controller = loader.getController();
            controller.setMainController(this); // Pour pouvoir rappeler Mainfirstclientcontroller

            Stage captureStage = new Stage();
            captureStage.setTitle("Prendre une Photo");
            captureStage.setScene(new Scene(root));
            captureStage.initModality(Modality.APPLICATION_MODAL);
            Stage owner = getStage();
            if (owner != null) captureStage.initOwner(owner);

            captureStage.showAndWait(); // Attendre que cette fenêtre soit fermée

            // Après fermeture, vérifier si une image a été capturée et mise dans attachmentService
            if (attachmentService != null && attachmentService.getCurrentAttachmentInfo() != null &&
                attachmentService.getCurrentAttachmentInfo().getFileType().startsWith("image/")) {
                displayAttachmentPreview(attachmentService.getCurrentAttachmentInfo());
                updateSendButtonState(messageField.getText(), true);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur UI", "Impossible d'ouvrir l'interface de capture caméra: " + e.getMessage());
        }
    }
	
	// Méthode pour afficher l'image capturée et préparer l'envoi
    // Appelée par CameraCaptureController
    public void processCapturedImage(File imageFile) {
        if (imageFile != null && imageFile.exists() && attachmentService != null) {
            try {
                AttachmentInfo imageAttachment = new AttachmentInfo(
                    imageFile.getName(),
                    Files.probeContentType(imageFile.toPath()), // Détecter le type MIME
                    imageFile.length(),
                    imageFile.getAbsolutePath()
                );
                attachmentService.setSelectedFileAndInfo(imageFile, imageAttachment);
                displayAttachmentPreview(imageAttachment); // Actualiser l'aperçu dans la barre de saisie
                updateSendButtonState(messageField.getText(), true); // Changer le bouton en "Envoyer"
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Erreur Fichier Image", "Impossible de traiter l'image capturée.");
            }
        }
    }

    private VBox createImageDisplayNode(Message message, AttachmentInfo imageAttachment, boolean isOutgoing) {
        VBox imageContainer = new VBox(5);
        imageContainer.getStyleClass().add("image-attachment-box");
        imageContainer.setAlignment(Pos.CENTER_LEFT); // Ou CENTER

        ImageView previewImageView = new ImageView();
        previewImageView.setPreserveRatio(true);
        // La largeur sera limitée par la classe CSS "media-message-bubble" sur le parent VBox messageBubbleContent
        previewImageView.setFitWidth(280); // Une largeur indicative pour le chargement, le CSS contrôlera
        previewImageView.setFitHeight(220);
        previewImageView.getStyleClass().add("chat-image-preview");

        File imageFile = null;
        // Essayer de trouver le fichier localement
        if (imageAttachment.getLocalPath() != null && new File(imageAttachment.getLocalPath()).exists()) {
            imageFile = new File(imageAttachment.getLocalPath());
        } else if (!isOutgoing) {
            imageFile = findDownloadedFile(message.getDatabaseId(), imageAttachment.getFileName(), imageAttachment.getFileSize());
        }

        if (imageFile != null && imageFile.exists()) {
            try {
                // Charger l'image en arrière-plan pour ne pas bloquer l'UI
                Image imageToLoad = new Image(imageFile.toURI().toString(), 280, 220, true, true, true); // true pour background loading
                previewImageView.setImage(imageToLoad);
                imageAttachment.setLocalPath(imageFile.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("Impossible de charger l'aperçu de l'image : " + imageFile.getPath() + " - " + e.getMessage());
                previewImageView.setImage(null); // Ou une image placeholder d'erreur
                Label errorLabel = new Label("Erreur image");
                imageContainer.getChildren().setAll(errorLabel); // Remplacer par le label d'erreur
                return imageContainer; // Retourner tôt
            }
        } else if (!isOutgoing) { // Image reçue non téléchargée
            Label downloadPrompt = new Label("Image (" + formatFileSize(imageAttachment.getFileSize()) + ")");
            Button downloadImageButton = new Button("⬇️");
            downloadImageButton.getStyleClass().add("download-image-button"); // Style spécifique si besoin

            downloadImageButton.setOnAction(e -> {
                downloadImageButton.setText("⏳"); downloadImageButton.setDisable(true);
                if (attachmentService != null) {
                    attachmentService.startFileDownload(message, getStage(),
                        () -> { // Success
                            File downloaded = new File(imageAttachment.getLocalPath()); // Path mis à jour
                            localDownloadedFiles.put(message.getDatabaseId() + "_" + imageAttachment.getFileName(), downloaded);
                            Platform.runLater(() -> {
                                imageContainer.getChildren().clear(); // Nettoyer le bouton de téléchargement
                                VBox newContent = createImageDisplayNode(message, imageAttachment, isOutgoing); // Recréer avec l'image
                                imageContainer.getChildren().addAll(newContent.getChildren());
                            });
                        },
                        () -> { // Failure
                            Platform.runLater(() -> {
                                imageContainer.getChildren().clear();
                                imageContainer.getChildren().add(new Label("Échec téléchargement"));
                            });
                        }
                    );
                }
            });
            imageContainer.getChildren().addAll(downloadPrompt, downloadImageButton);
            return imageContainer;
        } else { // Image sortante, fichier source introuvable
            imageContainer.getChildren().add(new Label("Source image perdue"));
            return imageContainer;
        }

        final File finalImageFileToOpen = imageFile;
        previewImageView.setOnMouseClicked(event -> {
            if (finalImageFileToOpen != null && finalImageFileToOpen.exists() && attachmentService != null) {
                attachmentService.openAttachment(finalImageFileToOpen.getAbsolutePath());
            }
        });

        // Méta-info sous l'image
        HBox imageMetaInfoBox = new HBox();
        Label imgTextLabel = new Label("Image");
        Pane spacer = new Pane(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Label imgSizeLabel = new Label(formatFileSize(imageAttachment.getFileSize()));
        imageMetaInfoBox.getChildren().addAll(imgTextLabel, spacer, imgSizeLabel);
        imageMetaInfoBox.getStyleClass().add("image-meta-info");

        imageContainer.getChildren().addAll(previewImageView, imageMetaInfoBox);
        return imageContainer;
    }
	 private void captureImageFromPreview() {
	        if (currentCameraFrame != null && !currentCameraFrame.empty()) currentCameraFrame.release();
	        currentCameraFrame = new Mat();

	        if (videoCapture != null && videoCapture.isOpened() && videoCapture.read(currentCameraFrame) && !currentCameraFrame.empty()) {
	            try {
	                MatOfByte matOfByte = new MatOfByte();
	                Imgcodecs.imencode(".png", currentCameraFrame, matOfByte);
	                byte[] byteArray = matOfByte.toArray();
	                matOfByte.release();

	                ensureDirectoryExists(attachmentService.getCurrentDefaultDownloadPath()); // 
	                File tempImageFile = File.createTempFile("cam_capture_" + System.currentTimeMillis() + "_", ".png", new File(attachmentService.getCurrentDefaultDownloadPath())); // 
	                Files.write(tempImageFile.toPath(), byteArray);

	                AttachmentInfo imageAttachment = new AttachmentInfo(
	                    tempImageFile.getName(), "image/png", tempImageFile.length(), tempImageFile.getAbsolutePath()
	                ); // 

	                if (attachmentService != null) {
	                    attachmentService.setSelectedFileAndInfo(tempImageFile, imageAttachment);
	                    this.displayAttachmentPreview(imageAttachment); // Utilise "this" ou appelle directement
	                    updateSendButtonState(messageField.getText(), true);
	                }
	                // showAlert(AlertType.INFORMATION, "Photo Prise", "Photo capturée et prête à être envoyée."); // Optionnel 
	            } catch (IOException e) {
	                e.printStackTrace(); // 
	                showAlert(AlertType.ERROR, "Erreur Capture", "Impossible de sauvegarder l'image capturée: " + e.getMessage()); // 
	            } finally {
	                if (currentCameraFrame != null) currentCameraFrame.release();
	            }
	        } else {
	            showAlert(AlertType.WARNING, "Capture Échouée", "Impossible de capturer une image valide de la caméra."); // 
	            if (currentCameraFrame != null) currentCameraFrame.release();
	        }
	        closeCameraPreview(); // 
	    }
	 
	 private void closeCameraPreview() {
	        isCameraActive = false;
	        // Ne pas relâcher videoCapture ici pour permettre de rouvrir la preview rapidement
	        // videoCapture.release(); videoCapture = null;
	        // Cela sera fait dans disconnectFromServer() ou à la fermeture de l'application
	        if (cameraPreviewStage != null && cameraPreviewStage.isShowing()) {
	            cameraPreviewStage.close();
	        }
	    }

	 private Image convertMatToFxImage(Mat mat) {
	        if (mat == null || mat.empty()) {
	            return null;
	        }
	        MatOfByte buffer = new MatOfByte();
	        Imgcodecs.imencode(".png", mat, buffer);
	        Image image = new Image(new ByteArrayInputStream(buffer.toArray()));
	        buffer.release();
	        return image;
	    }
	 
	 

	@FXML
	private void handleStartRecordingVoice(ActionEvent event) {
		if (isRecordingVoice)
			return;
		ClientDisplayWrapper selectedContactWrapper = contactListView.getSelectionModel().getSelectedItem();
		if (selectedContactWrapper == null || selectedContactWrapper.getClient() == null) {
			showAlert(AlertType.WARNING, "Aucun destinataire",
					"Veuillez sélectionner un contact pour enregistrer un message vocal.");
			return;
		}

		isRecordingVoice = true;
		updateStatus("Enregistrement vocal...");
		updateSendButtonState("",false); // Met à jour le bouton en mode "Arrêter"

		messageField.setDisable(true); // Optionnel: désactiver le champ texte
		if (attachmentPreviewPane != null) {
			attachmentPreviewPane.setVisible(false);
			attachmentPreviewPane.setManaged(false);
		}
		// Afficher le panneau d'enregistrement
		if (voiceRecordingPane != null) {
			voiceRecordingPane.setVisible(true);
			voiceRecordingPane.setManaged(true);
			if (recordingTimerLabel != null)
				recordingTimerLabel.setText("00:00");
			if (voiceVisualizer != null) {
				voiceVisualizer.getChildren().clear();
				for (int i = 0; i < 20; i++) {
					javafx.scene.shape.Rectangle bar = new javafx.scene.shape.Rectangle(4, 10); // Largeur, Hauteur
																								// initiale
					bar.setFill(javafx.scene.paint.Color.web("#7f8c8d")); // Couleur discrète
					voiceVisualizer.getChildren().add(bar);
				}
			}
		}

		try {
			// Format audio standard
			audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 1, 2, 16000, false);
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
			if (!AudioSystem.isLineSupported(info)) {
				showAlert(AlertType.ERROR, "Erreur Audio", "Ligne microphone non supportée pour ce format.");
				resetVoiceRecordingUI(true);
				return;
			}
			microphone = (TargetDataLine) AudioSystem.getLine(info);
			microphone.open(audioFormat);
			microphone.start();
			recordedAudioBytes = new ByteArrayOutputStream();
			recordingStartTime = System.currentTimeMillis();

			recordingThread = new Thread(() -> {
				byte[] buffer = new byte[microphone.getBufferSize() / 5];
				try {
					while (isRecordingVoice) {
						int bytesRead = microphone.read(buffer, 0, buffer.length);
						if (bytesRead > 0) {
							recordedAudioBytes.write(buffer, 0, bytesRead);
						}
						// Mettre à jour timer et visualiseur
						long elapsedMillis = System.currentTimeMillis() - recordingStartTime;
						Platform.runLater(() -> {
							if (recordingTimerLabel != null) {
								long totalSeconds = elapsedMillis / 1000;
								long secondsDisplay = totalSeconds % 60;
								long minutesDisplay = totalSeconds / 60;
								recordingTimerLabel.setText(String.format("%02d:%02d", minutesDisplay, secondsDisplay));
							}
							if (voiceVisualizer != null && !voiceVisualizer.getChildren().isEmpty()) {
								// Animation simple du visualiseur
								for (Node node : voiceVisualizer.getChildren()) {
									if (node instanceof javafx.scene.shape.Rectangle) {
										((javafx.scene.shape.Rectangle) node).setHeight(5 + (Math.random() * 20));
									}
								}
							}
						});
						Thread.sleep(50); // Fréquence de mise à jour
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					System.out.println("Thread d'enregistrement interrompu.");
				} catch (Exception e) {
					if (isRecordingVoice) {
						System.err.println("Erreur durant enregistrement: " + e.getMessage());
					}
				} finally {
					if (microphone != null) {
						microphone.stop();
						microphone.close();
					}
					// Appeler sur le thread UI pour les modifications d'UI
					Platform.runLater(() -> {
						if (!isRecordingVoice) { // Si l'arrêt n'a pas été déclenché par handleStop, c'est une erreur
							resetVoiceRecordingUIOnError();
						}
					});
				}
			});
			recordingThread.setDaemon(true);
			recordingThread.start();

		} catch (LineUnavailableException e) {
			showAlert(AlertType.ERROR, "Erreur Microphone", "Microphone non disponible: " + e.getMessage());
			resetVoiceRecordingUI(true);
		}
	}

	@FXML
	private void handleStopRecordingAndSendVoice(ActionEvent event) {
	    if (!isRecordingVoice) {
	         if (messageField != null && !messageField.getText().trim().isEmpty() && sendMessageButton != null && sendMessageButton.getText().equals(EMOJI_SEND_TEXT)){
	             handleSendMessageButton(event);
	         }
	         return;
	    }

	    isRecordingVoice = false;
	    updateStatus("Finalisation de l'enregistrement...");

	    Platform.runLater(() -> {
	        // ... (début de la méthode inchangé : join thread, vérification taille audio) ...
	        if (recordingThread != null) {
	            try {
	                recordingThread.join(300);
	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	            }
	        }

	        if (recordedAudioBytes == null || (audioFormat != null && recordedAudioBytes.size() < audioFormat.getFrameSize() * 10) ) {
	            showAlert(AlertType.WARNING, "Enregistrement Trop Court", "L'enregistrement vocal est trop court ou vide.");
	            resetVoiceRecordingUI(false);
	            return;
	        }

	        File tempAudioFile = null;
	        try {
	            // ... (création tempAudioFile et AttachmentInfo voiceAttachment comme avant) ...
	            ensureDirectoryExists(attachmentService.getCurrentDefaultDownloadPath()); // 
	            Path tempPath = Files.createTempFile(Paths.get(attachmentService.getCurrentDefaultDownloadPath()), "voice_" + System.currentTimeMillis() + "_", ".wav");
	            tempAudioFile = tempPath.toFile();

	            byte[] audioData = recordedAudioBytes.toByteArray();
	            ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
	            AudioInputStream ais = new AudioInputStream(bais, audioFormat, audioData.length / audioFormat.getFrameSize());
	            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, tempAudioFile);
	            ais.close(); bais.close();

	            AttachmentInfo voiceAttachment = new AttachmentInfo(
	                tempAudioFile.getName(), "audio/wav", tempAudioFile.length(), tempAudioFile.getAbsolutePath()
	            );

	            if (attachmentService != null) {
	                attachmentService.setSelectedFileAndInfo(tempAudioFile, voiceAttachment);
	            }

	            // **MODIFICATION : Le texte du messageField est récupéré par handleSendMessageButton**
	            // handleSendMessageButton va lire messageField.getText() et l'utiliser.
	            // S'il est vide, Message aura un contenu vide.
	            handleSendMessageButton(null); 
	            // Après l'appel à handleSendMessageButton, selectedFile et currentAttachmentInfo dans
	            // attachmentService sont normalement remis à null par cancelFileSelection() si l'envoi réussit.

	        } catch (IOException e) {
	            e.printStackTrace();
	            showAlert(AlertType.ERROR, "Erreur Fichier Vocal", "Impossible de sauvegarder ou préparer le fichier audio: " + e.getMessage());
	        } finally {
	            // Le reset de l'UI se fait à l'intérieur de handleSendMessageButton via cancelFileSelection()
	            // ou ici en cas d'erreur avant d'appeler handleSendMessageButton.
	            // On s'assure que l'état d'enregistrement est bien réinitialisé.
	            resetVoiceRecordingUI(true); // Effacer le messageField après l'envoi du vocal
	        }
	    });
	}
	private void resetVoiceRecordingUI(boolean clearMessageField) {
        isRecordingVoice = false;
        if (microphone != null && microphone.isOpen()) {
            microphone.stop();
            microphone.close();
            microphone = null; // 
        }
        recordedAudioBytes = null;

        if (messageField != null) {
            messageField.setDisable(false);
            if (clearMessageField) {
                // messageField.clear(); // Discutable, dépend si tu veux garder le texte
            }
        }
        if (voiceRecordingPane != null) {
            voiceRecordingPane.setVisible(false);
            voiceRecordingPane.setManaged(false); // 
        }
        updateSendButtonState(messageField != null ? messageField.getText() : "",
                              attachmentService != null && attachmentService.getCurrentAttachmentInfo() != null);
        updateStatus("Prêt."); // 
    }

	private void resetVoiceRecordingUIOnError() {
        Platform.runLater(() -> {
            isRecordingVoice = false;
            if (microphone != null && microphone.isOpen()) {
                microphone.stop();
                microphone.close();
                microphone = null;
            }
            recordedAudioBytes = null;

            if(messageField != null) messageField.setDisable(false);
            if (voiceRecordingPane != null) {
                voiceRecordingPane.setVisible(false);
                voiceRecordingPane.setManaged(false);
            }
            // **CORRECTION ICI**
            updateSendButtonState(messageField != null ? messageField.getText() : "",
                                  attachmentService != null && attachmentService.getCurrentAttachmentInfo() != null);
            // Ne pas changer updateStatus ici pour potentiellement garder un message d'erreur
        });
    }


	private VBox createVoiceMessageDisplayNode(Message message, AttachmentInfo voiceAttachment, boolean isOutgoing) {
		VBox voiceMessageContainer = new VBox(5); // Espacement global pour ce conteneur
		voiceMessageContainer.getStyleClass().add("voice-message-box");

		// 1. Titre "Message vocal"
		Label voiceTitleLabel = new Label("Message vocal");
		voiceTitleLabel.getStyleClass().add("voice-title-label"); // Nouvelle classe CSS

		// 2. Ligne pour Méta-informations (Durée et Taille)
		HBox metaInfoRow = new HBox();
		metaInfoRow.setAlignment(Pos.CENTER_LEFT); // Pour que les éléments soient sur la même ligne

		Label durationDisplayLabel = new Label("--:--"); // Placeholder, sera mis à jour
		durationDisplayLabel.getStyleClass().add("voice-meta-label");
		durationDisplayLabel.getStyleClass().add("voice-duration-static"); // Classe spécifique pour la durée statique

		Pane spacer = new Pane(); // Pour pousser la taille à droite
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Label sizeDisplayLabel = new Label(formatFileSize(voiceAttachment.getFileSize())); // Utilise ta méthode
																							// existante
		sizeDisplayLabel.getStyleClass().add("voice-meta-label");
		sizeDisplayLabel.getStyleClass().add("voice-size-label"); // Classe spécifique pour la taille

		metaInfoRow.getChildren().addAll(durationDisplayLabel, spacer, sizeDisplayLabel);

		// 3. Ligne pour les Contrôles de Lecture
		HBox controlsRow = new HBox(8); // Espacement entre bouton, barre, et timer de lecture
		controlsRow.setAlignment(Pos.CENTER_LEFT);

		Button playPauseBtn = new Button(); // L'icône sera définie par configurePlayPauseButtonForVoice
		// Le style du bouton vient de .voice-message-box .button dans le CSS

		javafx.scene.control.ProgressBar progressBar = new javafx.scene.control.ProgressBar(0);
		HBox.setHgrow(progressBar, Priority.ALWAYS); // La barre prend l'espace disponible

		Label playbackTimeLabel = new Label("00:00 / 00:00"); // Timer de lecture actuel / total
		playbackTimeLabel.getStyleClass().add("voice-playback-time-label"); // Nouvelle classe CSS

		// Logique pour le fichier local et le bouton de téléchargement (similaire à
		// avant)
		File localVoiceFile = null;
		if (isOutgoing) {
			// Pour les messages sortants, le fichier est celui qui vient d'être enregistré
			// ou sélectionné
			// Le chemin est dans voiceAttachment.getLocalPath()
			if (voiceAttachment.getLocalPath() != null) {
				localVoiceFile = new File(voiceAttachment.getLocalPath());
			}
			if (localVoiceFile == null || !localVoiceFile.exists()) {
				playPauseBtn.setText("⚠️"); // Source introuvable
				playPauseBtn.setDisable(true);
				durationDisplayLabel.setText("Source perdue"); // Mettre à jour le label de durée statique aussi
				playbackTimeLabel.setText(""); // Pas de temps de lecture si erreur
				System.err.println(
						"Fichier source pour message vocal sortant introuvable: " + voiceAttachment.getLocalPath());
			}
		} else { // Message entrant
			localVoiceFile = findDownloadedFile(message.getDatabaseId(), voiceAttachment.getFileName(),
					voiceAttachment.getFileSize()); //
			if (localVoiceFile == null || !localVoiceFile.exists()) {
				playPauseBtn.setText("⬇️"); // Télécharger
				playbackTimeLabel.setText(""); // Pas de temps de lecture avant téléchargement
				// La durée statique est déjà à "--:--" ou sera mise à jour après téléchargement
				playPauseBtn.setOnAction(e -> {
					playPauseBtn.setText("⏳"); // Chargement
					playPauseBtn.setDisable(true);
					if (attachmentService != null) {
						attachmentService.startFileDownload(message, getStage(), () -> { // Success
							File downloaded = new File(voiceAttachment.getLocalPath());
							localDownloadedFiles.put(message.getDatabaseId() + "_" + voiceAttachment.getFileName(),
									downloaded);
							Platform.runLater(() -> configurePlayPauseButtonForVoice(playPauseBtn, progressBar,
									playbackTimeLabel, durationDisplayLabel, downloaded));
						}, () -> { // Failure
							Platform.runLater(() -> {
								playPauseBtn.setText("⚠️");
								playbackTimeLabel.setText("Échec Tél.");
								playPauseBtn.setDisable(false);
							});
						});
					} else {
						System.err.println("AttachmentService non disponible pour télécharger le message vocal.");
						playPauseBtn.setText("⚠️");
						playbackTimeLabel.setText("Service Indisp.");
					}
				});
			}
		}

		if (localVoiceFile != null && localVoiceFile.exists()) {
			configurePlayPauseButtonForVoice(playPauseBtn, progressBar, playbackTimeLabel, durationDisplayLabel,
					localVoiceFile);
		}

		controlsRow.getChildren().addAll(playPauseBtn, progressBar, playbackTimeLabel);

		// Assembler le conteneur de message vocal
		voiceMessageContainer.getChildren().addAll(voiceTitleLabel, metaInfoRow, controlsRow);

		return voiceMessageContainer;
	}

	private void configurePlayPauseButtonForVoice(Button playPauseButton, javafx.scene.control.ProgressBar progressBar,
			Label playbackTimeLabel, // Pour le temps de lecture dynamique
			Label staticDurationLabel, // NOUVEAU: Pour afficher la durée totale sous "Message Vocal"
			File audioFile) {
		try {
			Media media = new Media(audioFile.toURI().toString());
			MediaPlayer mediaPlayer = new MediaPlayer(media);
			playPauseButton.setUserData(mediaPlayer); // Pour pouvoir le contrôler/stopper plus tard

			mediaPlayer.setOnError(() -> {
				System.err.println("MediaPlayer Error pour " + audioFile.getName() + ": " + mediaPlayer.getError());
				Platform.runLater(() -> {
					playPauseButton.setText("⚠️");
					playPauseButton.setDisable(true);
					staticDurationLabel.setText("Erreur");
					playbackTimeLabel.setText("Erreur lecture");
					showAlert(AlertType.ERROR, "Erreur Lecture Audio", "Impossible de lire le fichier audio : "
							+ audioFile.getName() + "\nErreur: "
							+ (mediaPlayer.getError() != null ? mediaPlayer.getError().getMessage() : "Inconnue"));
				});
			});

			mediaPlayer.setOnReady(() -> {
				Platform.runLater(() -> {
					if (media.getDuration() != null && !media.getDuration().isUnknown()
							&& media.getDuration().greaterThan(Duration.ZERO)) {
						double totalDuration = media.getDuration().toSeconds();
						long minutes = (long) (totalDuration / 60);
						long seconds = (long) (totalDuration % 60);
						String formattedTotalDuration = String.format("%02d:%02d", minutes, seconds);

						staticDurationLabel.setText(formattedTotalDuration); // Met à jour le label de durée statique
						playbackTimeLabel.setText("00:00 / " + formattedTotalDuration);

						playPauseButton.setText("▶️"); // Prêt à jouer
						playPauseButton.setDisable(false);
					} else {
						staticDurationLabel.setText("Durée N/A");
						playbackTimeLabel.setText("00:00 / Durée N/A");
						playPauseButton.setText("▶️"); // Permettre de tenter la lecture
						playPauseButton.setDisable(false);
						System.err.println("Durée du média inconnue ou nulle pour: " + audioFile.getName());
					}
				});
			});

			mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
				Platform.runLater(() -> {
					if (media.getDuration() != null && media.getDuration().greaterThan(Duration.ZERO)
							&& !media.getDuration().isUnknown()) {
						double currentTime = newTime.toSeconds();
						double totalDuration = media.getDuration().toSeconds();
						progressBar.setProgress(currentTime / totalDuration);

						long currentMinutes = (long) (currentTime / 60);
						long currentSeconds = (long) (currentTime % 60);
						long totalMinutes = (long) (totalDuration / 60);
						long totalSeconds = (long) (totalDuration % 60);
						playbackTimeLabel.setText(String.format("%02d:%02d / %02d:%02d", currentMinutes, currentSeconds,
								totalMinutes, totalSeconds));
					} else {
						progressBar.setProgress(0);
						playbackTimeLabel.setText("00:00 / Durée N/A");
					}
				});
			});

			mediaPlayer.setOnEndOfMedia(() -> {
				Platform.runLater(() -> {
					playPauseButton.setText("▶️"); // Replay
					progressBar.setProgress(0);
					if (media.getDuration() != null && !media.getDuration().isUnknown()) {
						double totalDuration = media.getDuration().toSeconds();
						long minutes = (long) (totalDuration / 60);
						long seconds = (long) (totalDuration % 60);
						playbackTimeLabel.setText(String.format("00:00 / %02d:%02d", minutes, seconds));
					} else {
						playbackTimeLabel.setText("00:00 / Durée N/A");
					}
					mediaPlayer.seek(Duration.ZERO);
					mediaPlayer.stop();
				});
			});

			playPauseButton.setOnAction(event -> {
				MediaPlayer.Status status = mediaPlayer.getStatus();
				if (status == MediaPlayer.Status.UNKNOWN || status == MediaPlayer.Status.HALTED) {
					System.err.println("MediaPlayer en état d'erreur ou non initialisé.");
					return;
				}

				if (status == MediaPlayer.Status.PAUSED || status == MediaPlayer.Status.READY
						|| status == MediaPlayer.Status.STOPPED || status == MediaPlayer.Status.STALLED) {
					mediaPlayer.play();
					playPauseButton.setText("⏸️");
				} else {
					mediaPlayer.pause();
					playPauseButton.setText("▶️");
				}
			});

		} catch (Exception e) { // MediaException, URISyntaxException, etc.
			System.err.println("Erreur grave lors de la configuration de MediaPlayer pour "
					+ (audioFile != null ? audioFile.getAbsolutePath() : "fichier null") + ": " + e.getMessage());
			e.printStackTrace();
			Platform.runLater(() -> {
				playPauseButton.setText("⚠️");
				playPauseButton.setDisable(true);
				staticDurationLabel.setText("Erreur");
				playbackTimeLabel.setText("Erreur fichier");
			});
		}
	}

	void ensureDirectoryExists(String pathStr) {
		Path path = Paths.get(pathStr);
		if (!Files.exists(path)) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				System.err.println("Impossible de créer le répertoire : " + pathStr + " - " + e.getMessage());
			}
		}
	}

	public void updatePeerInfo(String username, String host, int port, boolean isOnline) {
		Platform.runLater(() -> {
			contactOnlineStatusMap.put(username, isOnline);

			for (ClientDisplayWrapper wrapper : myPersonalContactsList) {
				if (wrapper.getClient().getNomUtilisateur().equals(username)) {
					wrapper.getClient().setLastKnownIp(host);
					wrapper.getClient().setLastKnownPort(port);
					break;
				}
			}
			contactListView.refresh();

			ClientDisplayWrapper selectedWrapper = contactListView.getSelectionModel().getSelectedItem();
			if (selectedWrapper != null && selectedWrapper.getClient().getNomUtilisateur().equals(username)) {
				updateChatHeaderWithPresence(selectedWrapper.getClient(), selectedWrapper.getDisplayName());
				if (isOnline && host != null && !host.isEmpty() && port > 0
						&& (!activePeerSessions.containsKey(username) || (activePeerSessions.get(username) != null
								&& !activePeerSessions.get(username).isConnected()))) {
					connectToPeer(username, host, port);
				}
			}
		});
	}

	private void updateChatHeaderWithPresence(Client contact, String displayName) {
		if (contact == null || contactChatHeaderLabel == null || presenceService == null) {
			if (contactChatHeaderLabel != null)
				contactChatHeaderLabel.setText("Sélectionnez un contact");
			return;
		}
		boolean isOnline = contactOnlineStatusMap.getOrDefault(contact.getNomUtilisateur(), false);
		String statusText = presenceService.getPresenceStatusForContact(contact.getId(), isOnline);
		contactChatHeaderLabel.setText("Discussion avec : " + displayName + " (" + statusText + ")");
	}

	private void startClientP2PServer() {
		if (currentUser == null || currentUser.getNomUtilisateur() == null || currentUser.getId() <= 0) {
			System.err.println("Impossible de démarrer le serveur P2P: infos utilisateur (nom/ID) incomplètes.");
			updateStatus("Erreur: Serveur P2P local non démarré (infos utilisateur manquantes).");
			return;
		}
		if (clientP2PServerThread != null && clientP2PServerThread.isAlive()) {
			System.out.println("Serveur P2P local déjà en cours d'exécution.");
			return;
		}
		if (userDAO == null)
			userDAO = new UserDAO();
		if (contactService == null) {
			contactService = new ContactService(new UserContactDAO(), userDAO);
			contactService.setMainController(this);
		}

		clientP2PServer = new ClientServer(currentUser.getNomUtilisateur(), currentUser.getId(), this, contactService,
				userDAO);
		clientP2PServerThread = new Thread(clientP2PServer);
		clientP2PServerThread.setDaemon(true);
		clientP2PServerThread.start();

		try {
			Thread.sleep(500);
		} catch (InterruptedException ignored) {
			Thread.currentThread().interrupt();
		}

		if (centralServerConnection != null && centralServerConnection.isConnected()) {
			String host = clientP2PServer.getHost();
			int port = clientP2PServer.getPort();
			if (port > 0) {
				centralServerConnection.notifyClientServerStarted(host, port);
			} else {
				showAlert(AlertType.ERROR, "Erreur Serveur P2P",
						"Port P2P local invalide ou serveur P2P non démarré correctement.");
			}
		} else {
			updateStatus("Serveur P2P local démarré, mais non connecté au serveur central pour notification.");
		}
	}

	public void disconnectFromServer() {
		updateStatus("Déconnexion en cours...");
		if (presenceService != null && currentUser != null && currentUser.getId() > 0) {
			presenceService.userDisconnected(currentUser.getId(), "hors-ligne");
		}
		if (scheduledExecutor != null && !scheduledExecutor.isShutdown()) {
			scheduledExecutor.shutdownNow();
			try {
				if (!scheduledExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
					System.err.println("ScheduledExecutor ne s'est pas arrêté proprement.");
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		new ArrayList<>(activePeerSessions.values()).forEach(PeerSession::close);
		activePeerSessions.clear();
		if (clientP2PServer != null)
			clientP2PServer.stop();
		if (clientP2PServerThread != null && clientP2PServerThread.isAlive()) {
			clientP2PServerThread.interrupt();
			try {
				clientP2PServerThread.join(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		if (centralServerConnection != null && centralServerConnection.isConnected())
			centralServerConnection.disconnect();

		updateStatus("Déconnecté.");
		Platform.runLater(() -> {
			try {
				Stage stage = (Stage) (statusLabel != null && statusLabel.getScene() != null
						? statusLabel.getScene().getWindow()
						: Stage.getWindows().get(0));
				Parent loginRoot = FXMLLoader
						.load(getClass().getResource("/com/Alanya/Interfaceauthentification.fxml"));
				stage.setScene(new Scene(loginRoot));
				stage.setTitle("ALANYA - Connexion");
				stage.centerOnScreen();
				stage.show();
			} catch (Exception e) {
				e.printStackTrace();
				Platform.exit();
				System.exit(0);
			}
		});
	}

	@FXML
	void handleLogoutButton(ActionEvent event) {
		disconnectFromServer();
	}

	public void stopCapture() {
		System.out.println(
				"Mainfirstclientcontroller.stopCapture() appelé. (Placeholder pour arrêt capture vidéo/audio globale)");
		if (videoCapture != null && videoCapture.isOpened()) {
			videoCapture.release();
		}
		if (microphone != null && microphone.isOpen()) {
			microphone.close();
		}
	}

	public void updateStatus(String text) {
		Platform.runLater(() -> {
			if (statusLabel != null)
				statusLabel.setText(text);
		});
	}

	private void handleEnterKeyPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			handleSendMessageButton(null);
			event.consume();
		}
	}

	@FXML
    void handleSendMessageButton(ActionEvent event) {
        String content = messageField.getText().trim();
        ClientDisplayWrapper selectedContactWrapper = contactListView.getSelectionModel().getSelectedItem();

        if (selectedContactWrapper == null) {
            showAlert(AlertType.WARNING, "Aucun destinataire", "Veuillez sélectionner un contact.");
            return;
        }
        if (content.isEmpty() && (attachmentService == null || attachmentService.getCurrentAttachmentInfo() == null)) {
            return;
        }

        Client selectedContact = selectedContactWrapper.getClient();
        Message messageToSend = new Message(currentUser.getNomUtilisateur(), selectedContact.getNomUtilisateur(), content);
        
        // Gérer la pièce jointe
        if (attachmentService != null && attachmentService.getCurrentAttachmentInfo() != null) {
            AttachmentInfo attachmentInfo = attachmentService.getCurrentAttachmentInfo();
            File fileToSend = attachmentService.getSelectedFile();
            if (fileToSend != null && fileToSend.exists()) {
                try {
                    byte[] fileData = Files.readAllBytes(fileToSend.toPath());
                    attachmentInfo.setFileData(fileData);
                    messageToSend.setAttachmentInfo(attachmentInfo);
                } catch (IOException e) {
                    showAlert(AlertType.ERROR, "Erreur Fichier", "Impossible de lire le fichier.");
                    return;
                }
            }
        }

        // 1. Sauvegarder le message dans la BDD. C'est maintenant l'action principale.
        storeSentMessageInDB(messageToSend, currentUser.getId(), selectedContact.getId());
        if (messageToSend.getDatabaseId() <= 0) {
            showAlert(AlertType.ERROR, "Erreur d'envoi", "Impossible de sauvegarder le message avant l'envoi.");
            return;
        }

        // 2. Afficher le message immédiatement dans notre propre interface.
        displayMessage(messageToSend, true);
        messageField.clear();
        if (attachmentService != null) {
            attachmentService.cancelFileSelection();
        }
        
        // 3. Essayer d'envoyer en P2P si le pair est connecté.
        PeerSession peerSession = activePeerSessions.get(selectedContact.getNomUtilisateur());
        if (peerSession != null && peerSession.isConnected()) {
            peerSession.sendMessage(messageToSend);
            System.out.println("Message envoyé en temps réel à " + selectedContact.getNomUtilisateur());
        } else {
            // Si non connecté, c'est tout bon. Le message est déjà dans la BDD.
            System.out.println("Destinataire hors ligne. Message stocké pour livraison ultérieure.");
        }
    }
    
	private void checkForOfflineMessages() {
        if (currentUser == null || currentUser.getId() <= 0) return;

        new Thread(() -> {
            try {
                MessageDAO dao = new MessageDAO();
                List<Message> offlineMessages = dao.getOfflineMessagesForUser(currentUser.getId());

                if (!offlineMessages.isEmpty()) {
                    System.out.println("Vous avez " + offlineMessages.size() + " message(s) non lus reçu(s) hors ligne.");
                    
                    // Récupérer les IDs des messages pour les marquer comme "reçus"
                    List<Long> messageIdsToUpdate = offlineMessages.stream()
                                                                   .map(Message::getDatabaseId)
                                                                   .collect(Collectors.toList());
                    dao.updateMessagesStatusToDelivered(messageIdsToUpdate);

                    // Mettre à jour les notifications dans l'UI
                    Platform.runLater(() -> {
                        notificationService.loadInitialUnreadCounts(currentUser.getId());
                        contactListView.refresh();
                        showAlert(AlertType.INFORMATION, "Nouveaux Messages", "Vous avez reçu " + offlineMessages.size() + " nouveau(x) message(s).");
                    });
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

	public boolean isChattingWith(String username) {
        ClientDisplayWrapper selectedContactWrapper = contactListView.getSelectionModel().getSelectedItem();
        if (selectedContactWrapper != null && selectedContactWrapper.getClient() != null) {
            return selectedContactWrapper.getClient().getNomUtilisateur().equals(username);
        }
        return false;
    }
	@FXML
	public void handleBack(ActionEvent event) {
		contactListView.getSelectionModel().clearSelection();

		if (messagesScrollPane != null) {
			messagesScrollPane.setVisible(false);
			messagesScrollPane.setManaged(false);
		}
		if (messageInputContainer != null) {
			messageInputContainer.setVisible(false);
			messageInputContainer.setManaged(false);
		}
		if (contactNAMEHBOX != null) {
			contactNAMEHBOX.setVisible(false);
			contactNAMEHBOX.setManaged(false);
		}

		if (defaultConversationImage != null) {
			defaultConversationImage.setVisible(true);
			defaultConversationImage.setManaged(true);
		}

		if (messagesContainerVBox != null) {
			messagesContainerVBox.getChildren().clear();
		}
		if (attachmentService != null) {
			attachmentService.cancelFileSelection();
			displayAttachmentPreview(null);
		}
		System.out.println("handleBack: UI réinitialisée à l'état d'accueil du chat.");
	}

	@FXML
	void handleAttachment(ActionEvent event) {
		Stage currentStage = getStage();
		if (currentStage != null && attachmentService != null) {
			attachmentService.selectFile(currentStage);
		} else {
			showAlert(AlertType.ERROR, "Erreur",
					"Impossible d'ouvrir le sélecteur de fichiers (Stage ou service non disponible).");
		}
	}

	 public void displayAttachmentPreview(AttachmentInfo info) {
	    	if (info != null && attachmentPreviewPane != null && attachmentFileNameLabel != null) { // 
	    		attachmentFileNameLabel.setText(info.getFileName() + " (" + formatFileSize(info.getFileSize()) + ")"); // 
	            attachmentPreviewPane.setVisible(true); // 
	            attachmentPreviewPane.setManaged(true); // 
	        } else if (attachmentPreviewPane != null) { 
	            attachmentPreviewPane.setVisible(false); // 
	            attachmentPreviewPane.setManaged(false); // 
	             if (attachmentFileNameLabel != null) attachmentFileNameLabel.setText(""); // 
	        } else {
	             System.err.println("Impossible d'afficher la prévisualisation de la pièce jointe - attachmentPreviewPane ou attachmentFileNameLabel est null."); // 
	        }
	        updateSendButtonStateBasedOnAttachment();
	    }

	@FXML
	void handleCancelAttachment(ActionEvent event) {
		if (attachmentService != null) {
			attachmentService.cancelFileSelection();
		}
	}

	@FXML
	void handleNewContact(ActionEvent event) {
		if (this.currentUser == null || this.currentUser.getId() <= 0) {
			showAlert(AlertType.WARNING, "Action impossible", "Vous devez être connecté pour ajouter un contact.");
			return;
		}
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/Alanya/EditContactView.fxml"));
			Parent root = loader.load();
			Addclientcontroller addController = loader.getController();
			addController.setMode("contact");
			addController.setCurrentUserId(this.currentUser.getId());

			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Ajouter Nouveau Contact");
			Node sourceNode = contactListView;
			if (sourceNode != null && sourceNode.getScene() != null) {
				stage.initOwner(sourceNode.getScene().getWindow());
			}

			stage.setScene(new Scene(root, 480, 300));
			stage.showAndWait();

			Client newContact = addController.getNewRegisteredClient();
			if (newContact != null) {
				loadMyPersonalContacts();
			}
		} catch (IOException e) {
			e.printStackTrace();
			showAlert(AlertType.ERROR, "Erreur Interface",
					"Impossible d'ouvrir l'interface d'ajout de contact : " + e.getMessage());
		}
	}

	public void peerDisconnected(String username) {
		Platform.runLater(() -> {
			updateStatus("Pair " + username + " déconnecté de votre serveur P2P.");
			System.out.println("Notification UI: Pair " + username + " s'est déconnecté de notre serveur P2P.");
			contactOnlineStatusMap.put(username, false);
			contactListView.refresh();

			ClientDisplayWrapper selectedWrapper = contactListView.getSelectionModel().getSelectedItem();
			if (selectedWrapper != null && selectedWrapper.getClient() != null
					&& selectedWrapper.getClient().getNomUtilisateur().equals(username)) {
				updateChatHeaderWithPresence(selectedWrapper.getClient(), selectedWrapper.getDisplayName());
			}
		});
	}

	public void handleCentralServerAuthenticated(String username, int userIdFromServer) {
		if (this.currentUser == null || !this.currentUser.getNomUtilisateur().equals(username)) {
			this.currentUser = findClientInSystemByUsername(username);
			if (this.currentUser == null) {
				this.currentUser = new Client();
				this.currentUser.setNomUtilisateur(username);
			}
		}
		if (userIdFromServer > 0) {
			this.currentUser.setId(userIdFromServer);
		}
		System.out.println("Authentifié par serveur central: " + currentUser.getNomUtilisateur() + " (ID: "
				+ currentUser.getId() + ")");

		if (currentUserLabel != null && this.currentUser != null) {
			currentUserLabel.setText("COMPTE DE : " + this.currentUser.getNomUtilisateur());
		}

		startClientP2PServer();
		loadMyPersonalContacts();

		if (notificationService != null && this.currentUser != null && this.currentUser.getId() > 0) {
			notificationService.loadInitialUnreadCounts(currentUser.getId());
		} else {
			System.err.println(
					"handleCentralServerAuthenticated: NotificationService ou currentUser invalide pour charger les notifications.");
		}

		if (scheduledExecutor != null && !scheduledExecutor.isShutdown()) {
			try {
				scheduledExecutor.scheduleAtFixedRate(this::refreshContactStatuses, 15, 45, TimeUnit.SECONDS);
			} catch (Exception e) {
				System.err.println("Impossible de planifier refreshContactStatuses: " + e.getMessage());
			}
		}
		Platform.runLater(() -> {
            if (currentUser != null) {
                currentUsernameLabel.setText(currentUser.getNomUtilisateur());
                Image profileImg = bytesToImage(currentUser.getProfilePicture());
                currentUserAvatar.setFill(new ImagePattern(profileImg != null ? profileImg : defaultAvatar));
            }
        });
		refreshContactStatuses();
	}

    private Image bytesToImage(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            return new Image(is);
        } catch (Exception e) {
            System.err.println("Erreur conversion bytes en image: " + e.getMessage());
            return null;
        }
    }
    
    
	public void peerConnected(String username) {
		Platform.runLater(() -> {
			updateStatus("Pair " + username + " connecté à votre serveur P2P.");
			System.out.println("Notification UI: Pair " + username + " s'est connecté à notre serveur P2P.");
		});
	}

	private Client findClientInSystemByUsername(String username) {
		if (username == null || username.trim().isEmpty())
			return null;
		if (currentUser != null && currentUser.getNomUtilisateur().equals(username))
			return currentUser;
		for (ClientDisplayWrapper wrapper : myPersonalContactsList) {
			if (wrapper.getClient().getNomUtilisateur().equals(username))
				return wrapper.getClient();
		}
		return findClientInDBByUsername(username);
	}

	public void showAlert(String title, String content) {
		showAlert(AlertType.INFORMATION, title, content);
	}

	public void showAlert(AlertType type, String title, String content) {
		Platform.runLater(() -> {
			Alert alert = new Alert(type);
			alert.setTitle(title);
			alert.setHeaderText(null);
			alert.setContentText(content);
			alert.showAndWait();
		});
	}

	private void showAlert(AlertType type, String title, String content, Runnable onOkAction) {
		Platform.runLater(() -> {
			Alert alert = new Alert(type);
			alert.setTitle(title);
			alert.setHeaderText(null);
			alert.setContentText(content);
			if (type == AlertType.CONFIRMATION) {
				alert.showAndWait().ifPresent(response -> {
					if (response == ButtonType.OK)
						onOkAction.run();
				});
			} else {
				alert.showAndWait();
				if (onOkAction != null)
					onOkAction.run();
			}
		});
	}

	private Client findClientInDBByUsername(String username) {
	    if (userDAO == null) {
	        System.err.println("UserDAO non initialisé.");
	        return null;
	    }
	    try {
	        return userDAO.findUserByUsername(username);
	    } catch (SQLException e) {
	        System.err.println("Erreur lors de la recherche du client par nom '" + username + "': " + e.getMessage());
	        return null;
	    }
	}
	
	public void setAuthenticatedUser(Client authenticatedUser, String password) {
	    this.currentUser = authenticatedUser;
	    this.clientPassword = password;

	    System.out.println("Identifiants client définis : "
	            + (this.currentUser != null ? this.currentUser.getNomUtilisateur() : "null") + " (ID: "
	            + (this.currentUser != null ? this.currentUser.getId() : "N/A") + ")");

	    Platform.runLater(() -> {
	        if (currentUser != null && currentUsernameLabel != null && currentUserAvatar != null) {
	            // Mettre à jour le nom d'utilisateur dans le label
	            currentUsernameLabel.setText(currentUser.getNomUtilisateur());
	            
	            // Mettre à jour la photo de profil dans le cercle
	            updateProfilePictureUI(bytesToImage(currentUser.getProfilePicture()));
	        }
	    });
	}
	@FXML
	void handleAudioCall(ActionEvent event) {
	    ClientDisplayWrapper selectedWrapper = contactListView.getSelectionModel().getSelectedItem();
	    if (selectedWrapper == null || selectedWrapper.getClient() == null) {
	        showAlert(AlertType.WARNING, "Aucun contact sélectionné", "Veuillez sélectionner un contact pour démarrer un appel audio.");
	        return;
	    }
	    Client selectedContact = selectedWrapper.getClient();

	    // VÉRIFICATION DU STATUT EN LIGNE
	    boolean isOnline = contactOnlineStatusMap.getOrDefault(selectedContact.getNomUtilisateur(), false);
	    if (!isOnline) {
	        showAlert(AlertType.INFORMATION, "Utilisateur Hors Ligne", selectedWrapper.getDisplayName() + " n'est pas connecté(e) et ne peut pas recevoir d'appels.");
	        return;
	    }

	    if (isInCall) {
	        showAlert(AlertType.INFORMATION, "Appel en cours", "Vous êtes déjà dans un appel. Veuillez terminer l'appel actuel avant d'en démarrer un nouveau.");
	        return;
	    }

	    this.activeCallPartner = selectedContact.getNomUtilisateur();
	    this.callType = "audio";
	    this.activeCallId = currentUser.getNomUtilisateur() + "_" + activeCallPartner + "_" + System.currentTimeMillis();

	    Map<String, String> callData = new HashMap<>();
	    callData.put("targetUsername", activeCallPartner);
	    callData.put("callId", activeCallId);
	    if (centralServerConnection != null && centralServerConnection.isConnected()) {
	        centralServerConnection.sendCommand(new ServerCommand(ServerCommand.ServerCommandType.INITIATE_AUDIO_CALL, callData));
	        displayCallStatusMessage("Demande d'appel audio à " + selectedWrapper.getDisplayName() + "...", "pending", "audio");
	        saveCallEventToDatabase(currentUser.getId(), selectedContact.getId(), activeCallId, "audio_request_sent");
	        updateStatus("Demande d'appel audio envoyée à " + selectedWrapper.getDisplayName());
	    } else {
	        showAlert(AlertType.ERROR, "Erreur Connexion", "Non connecté au serveur central pour initier l'appel.");
	    }
	}

	@FXML
	void handleVideoCall(ActionEvent event) {
	    ClientDisplayWrapper selectedWrapper = contactListView.getSelectionModel().getSelectedItem();
	    if (selectedWrapper == null || selectedWrapper.getClient() == null) {
	        showAlert(AlertType.WARNING, "Aucun contact sélectionné", "Veuillez sélectionner un contact pour démarrer un appel vidéo.");
	        return;
	    }
	    Client selectedContact = selectedWrapper.getClient();

	    // VÉRIFICATION DU STATUT EN LIGNE
	    boolean isOnline = contactOnlineStatusMap.getOrDefault(selectedContact.getNomUtilisateur(), false);
	    if (!isOnline) {
	        showAlert(AlertType.INFORMATION, "Utilisateur Hors Ligne", selectedWrapper.getDisplayName() + " n'est pas connecté(e) et ne peut pas recevoir d'appels.");
	        return;
	    }

	    if (isInCall) {
	        showAlert(AlertType.INFORMATION, "Appel en cours", "Vous êtes déjà dans un appel.");
	        return;
	    }

	    this.activeCallPartner = selectedContact.getNomUtilisateur();
	    this.callType = "video";
	    this.activeCallId = currentUser.getNomUtilisateur() + "_" + activeCallPartner + "_" + System.currentTimeMillis();

	    Map<String, String> callData = new HashMap<>();
	    callData.put("targetUsername", activeCallPartner);
	    callData.put("callId", activeCallId);
	    if (centralServerConnection != null && centralServerConnection.isConnected()) {
	        centralServerConnection.sendCommand(new ServerCommand(ServerCommand.ServerCommandType.INITIATE_VIDEO_CALL, callData));
	        displayCallStatusMessage("Demande d'appel vidéo à " + selectedWrapper.getDisplayName() + "...", "pending", "video");
	        saveCallEventToDatabase(currentUser.getId(), selectedContact.getId(), activeCallId, "video_request_sent");
	        updateStatus("Demande d'appel vidéo envoyée à " + selectedWrapper.getDisplayName());
	    } else {
	        showAlert(AlertType.ERROR, "Erreur Connexion", "Non connecté au serveur central pour initier l'appel.");
	    }
	}


	public void handleIncomingCall(String callerUsername, String callId, String type) {
	    if (isInCall) {
	        Map<String, String> responseData = new HashMap<>();
	        responseData.put("callId", callId);
	        responseData.put("responderUsername", currentUser.getNomUtilisateur());
	        responseData.put("accepted", "false");
	        responseData.put("type", type);
	        responseData.put("reason", "busy");
	        if (centralServerConnection != null && centralServerConnection.isConnected()) {
	            centralServerConnection.sendCommand(new ServerCommand(ServerCommand.ServerCommandType.CALL_RESPONSE, responseData));
	        }
	        return;
	    }

	    String callerDisplayName = callerUsername;
	    for (ClientDisplayWrapper wrapper : myPersonalContactsList) {
	        if (wrapper.getClient().getNomUtilisateur().equals(callerUsername)) {
	            callerDisplayName = wrapper.getDisplayName();
	            break;
	        }
	    }

	    final String finalCallerDisplayName = callerDisplayName;
	    Platform.runLater(() -> {
	        Alert alert = new Alert(AlertType.CONFIRMATION);
	        alert.setTitle("Appel " + type + " Entrant");
	        alert.setHeaderText(finalCallerDisplayName + " vous appelle (" + type + ").");
	        alert.setContentText("Voulez-vous accepter ?");

	        Optional<ButtonType> result = alert.showAndWait();
	        boolean accepted = result.isPresent() && result.get() == ButtonType.OK;

	        Map<String, String> responseData = new HashMap<>();
	        responseData.put("callId", callId);
	        responseData.put("responderUsername", currentUser.getNomUtilisateur());
	        responseData.put("accepted", String.valueOf(accepted));
	        responseData.put("type", type);

	        if (accepted) {
	            this.activeCallId = callId;
	            this.activeCallPartner = callerUsername;
	            this.isInCall = true;
	            this.callType = type;

	            if ("video".equals(type)) {
	                Map<String, Integer> ports = startP2PServersForVideoCall();
	                if (ports == null) {
	                    showAlert(AlertType.ERROR, "Erreur d'appel", "Impossible de démarrer les serveurs P2P locaux pour l'appel vidéo.");
	                    responseData.put("accepted", "false");
	                    responseData.put("reason", "p2p_setup_failed");
	                    if (centralServerConnection != null) centralServerConnection.sendCommand(new ServerCommand(ServerCommand.ServerCommandType.CALL_RESPONSE, responseData));
	                    resetCallState();
	                    return;
	                }
	                responseData.put("audioPort", String.valueOf(ports.get("audioPort")));
	                responseData.put("videoPort", String.valueOf(ports.get("videoPort")));
	                showVideoCallWindow(finalCallerDisplayName, false);
	                startCallTimer(videoCallController.getTimerLabel());
	            } else { // Appel audio
	                int localP2PPort = startP2PServerForCall(type);
	                 if (localP2PPort == -1) {
	                    showAlert(AlertType.ERROR, "Erreur d'appel", "Impossible de démarrer le serveur P2P local pour l'appel audio.");
	                    responseData.put("accepted", "false");
	                    responseData.put("reason", "p2p_setup_failed");
	                    if (centralServerConnection != null) centralServerConnection.sendCommand(new ServerCommand(ServerCommand.ServerCommandType.CALL_RESPONSE, responseData));
	                    resetCallState();
	                    return;
	                }
	                responseData.put("port", String.valueOf(localP2PPort));
	                showAudioCallWindow(finalCallerDisplayName, false);
	                startCallTimer(audioCallController.getTimerLabel());
	            }

	            displayCallStatusMessage("Appel " + type + " avec " + finalCallerDisplayName + " en cours...", "active", type);
	            saveCallEventToDatabase(currentUser.getId(), getContactIdByUsername(callerUsername), activeCallId, type + "_accepted_incoming");
	        } else {
	            displayCallStatusMessage("Appel " + type + " de " + finalCallerDisplayName + " refusé.", "rejected", type);
	            saveCallEventToDatabase(currentUser.getId(), getContactIdByUsername(callerUsername), callId, type + "_rejected_incoming");
	        }
	        
	        if (centralServerConnection != null && centralServerConnection.isConnected()) {
	            centralServerConnection.sendCommand(new ServerCommand(ServerCommand.ServerCommandType.CALL_RESPONSE, responseData));
	        }
	    });
	}
	private void displayCallStatusMessage(String message, String statusType, String callTypeAssoc) {
	    Platform.runLater(() -> {
	        HBox messageRow = new HBox();
	        messageRow.setAlignment(Pos.CENTER);
	        messageRow.setPadding(new Insets(5, 10, 5, 10));

	        Label statusLabelText = new Label(message); // Message est maintenant complet
	        String icon = "";

	        if (!message.toUpperCase().contains("APPEL TERMINÉ")) {
	             if ("audio".equals(callTypeAssoc)) {
	                icon = (statusType.equals("ended") || statusType.equals("rejected") || statusType.equals("cancelled")) ? EMOJI_PHONE_RED_BARRED : EMOJI_PHONE_GREEN;
	            } else if ("video".equals(callTypeAssoc)) {
	                icon = (statusType.equals("ended") || statusType.equals("rejected") || statusType.equals("cancelled")) ? EMOJI_CAMERA_RED_BARRED : EMOJI_CAMERA_GREEN;
	            }
	            statusLabelText.setText(icon + " " + message);
	        }

	        if (statusType.equals("active") || statusType.equals("pending") || statusType.equals("unmuted")) {
	            statusLabelText.setStyle("-fx-text-fill: green; -fx-font-style: italic; -fx-padding: 3px; -fx-background-color: #e6ffe6; -fx-background-radius: 5;");
	        } else {
	            statusLabelText.setStyle("-fx-text-fill: #555; -fx-font-style: italic; -fx-padding: 3px; -fx-background-color: #f0f0f0; -fx-background-radius: 5;");
	        }
	        
	        messageRow.getChildren().add(statusLabelText);
	        messagesContainerVBox.getChildren().add(messageRow);

	        if (messagesScrollPane != null) {
	            messagesScrollPane.setVvalue(1.0);
	        }
	    });
	}
	
	
	private Map<String, Integer> startP2PServersForVideoCall() {
	    try {
	        // Nettoyage des anciens sockets serveur
	        if (p2pAudioServerSocket != null && !p2pAudioServerSocket.isClosed()) p2pAudioServerSocket.close();
	        if (p2pVideoServerSocket != null && !p2pVideoServerSocket.isClosed()) p2pVideoServerSocket.close();

	        // Création de deux serveurs sockets distincts
	        p2pAudioServerSocket = new ServerSocket(0);
	        p2pVideoServerSocket = new ServerSocket(0);

	        int audioPort = p2pAudioServerSocket.getLocalPort();
	        int videoPort = p2pVideoServerSocket.getLocalPort();
	        System.out.println("Serveurs P2P (Récepteur) prêts sur AUDIO:" + audioPort + ", VIDEO:" + videoPort);

	        // Thread pour la connexion audio
	        p2pAudioThread = new Thread(() -> {
	            try {
	                // 1. Attendre la connexion de l'appelant sur le port audio
	                p2pAudioSocket = p2pAudioServerSocket.accept();
	                // 2. Initialiser les flux pour ce canal
	                p2pAudioIn = new ObjectInputStream(p2pAudioSocket.getInputStream());
	                p2pAudioOut = new ObjectOutputStream(p2pAudioSocket.getOutputStream());
	                p2pAudioOut.flush();
	                System.out.println("Récepteur: Connexion audio P2P acceptée de: " + p2pAudioSocket.getInetAddress());

	                // 3. DÉMARRER LA RÉCEPTION ET L'ENVOI AUDIO SEULEMENT APRÈS CONNEXION
	                startAudioReception();
	                startAudioStreaming();
	            } catch (IOException e) {
	                if (isInCall) endCallLogic(false);
	            }
	        });

	        // Thread pour la connexion vidéo
	        p2pVideoThread = new Thread(() -> {
	            try {
	                // 1. Attendre la connexion de l'appelant sur le port vidéo
	                p2pVideoSocket = p2pVideoServerSocket.accept();
	                // 2. Initialiser les flux pour ce canal
	                p2pVideoIn = new ObjectInputStream(p2pVideoSocket.getInputStream());
	                p2pVideoOut = new ObjectOutputStream(p2pVideoSocket.getOutputStream());
	                p2pVideoOut.flush();
	                System.out.println("Récepteur: Connexion vidéo P2P acceptée de: " + p2pVideoSocket.getInetAddress());

	                // 3. DÉMARRER LA RÉCEPTION ET L'ENVOI VIDÉO SEULEMENT APRÈS CONNEXION
	                startVideoReception();
	                startVideoStreaming();
	            } catch (IOException e) {
	                if (isInCall) endCallLogic(false);
	            }
	        });

	        p2pAudioThread.setDaemon(true);
	        p2pVideoThread.setDaemon(true);
	        p2pAudioThread.start();
	        p2pVideoThread.start();

	        Map<String, Integer> ports = new HashMap<>();
	        ports.put("audioPort", audioPort);
	        ports.put("videoPort", videoPort);
	        return ports;
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}

	public void handleCallAcceptedByPeerVideo(String responderUsername, String callId, String peerIp, int peerAudioPort, int peerVideoPort) {
	    if (this.activeCallId == null || !this.activeCallId.equals(callId)) {
	        return;
	    }
	    this.isInCall = true;
	    updateStatus("Appel vidéo accepté. Connexion P2P à " + responderUsername + "...");
	    String peerDisplayName = getDisplayNameForUser(responderUsername);

	    new Thread(() -> {
	        try {
	            // Se connecter aux deux sockets du récepteur
	            p2pAudioSocket = new Socket(peerIp, peerAudioPort);
	            p2pVideoSocket = new Socket(peerIp, peerVideoPort);

	            // Initialiser tous les flux de sortie d'abord pour éviter un blocage
	            p2pAudioOut = new ObjectOutputStream(p2pAudioSocket.getOutputStream());
	            p2pAudioOut.flush();
	            p2pVideoOut = new ObjectOutputStream(p2pVideoSocket.getOutputStream());
	            p2pVideoOut.flush();
	            
	            // Puis initialiser les flux d'entrée
	            p2pAudioIn = new ObjectInputStream(p2pAudioSocket.getInputStream());
	            p2pVideoIn = new ObjectInputStream(p2pVideoSocket.getInputStream());
	            
	            System.out.println("Appelant: Connecté aux deux canaux P2P.");

	            Platform.runLater(() -> {
	                showVideoCallWindow(peerDisplayName, true);
	                startCallTimer(videoCallController.getTimerLabel());
	            });

	            // Démarrer tous les flux après l'établissement des connexions
	            startAudioReception();
	            startVideoReception();
	            startAudioStreaming();
	            startVideoStreaming();
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	            Platform.runLater(() -> {
	                showAlert(AlertType.ERROR, "Erreur P2P", "Impossible de se connecter en P2P (vidéo) avec " + peerDisplayName + ".");
	                endCallLogic(true);
	            });
	        }
	    }).start();
	}

	// Ajouter cette nouvelle méthode (ou l'intégrer dans startVideoStreaming)
	private void startCombinedVideoAudioReception() {
	     if (p2pVideoIn == null) {
	         System.err.println("COMB_RECV: Impossible de démarrer la réception, p2pVideoIn est null.");
	         return;
	     }

	     new Thread(() -> {
	         try {
	             if (speakers == null || !speakers.isOpen()) {
	                 speakers = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, STANDARD_AUDIO_FORMAT));
	                 speakers.open(STANDARD_AUDIO_FORMAT);
	                 speakers.start();
	             }
	             
	             while (isInCall && (videoStreaming || audioStreaming)) {
	                 Object receivedObject = p2pVideoIn.readObject();

	                 if (receivedObject instanceof VideoPacket && videoStreaming) {
	                     // ... (logique de traitement VideoPacket inchangée)
	                 } else if (receivedObject instanceof AudioPacket && audioStreaming) {
	                     AudioPacket audioPkt = (AudioPacket) receivedObject;
	                     if (speakers != null && speakers.isOpen()) {
	                         speakers.write(audioPkt.getData(), 0, audioPkt.getLength());
	                     }
	                 }
	             }
	         } catch (Exception e) {
	             if (isInCall) {
	                System.out.println("COMB_RECV: Le pair a fermé le flux ou une erreur est survenue: " + e.getMessage());
	                Platform.runLater(() -> endCallLogic(false));
	             }
	         }
	     }, "P2P-CombinedReceiveThread").start();
	}


	// Ajouter cette nouvelle méthode
	private void startCallTimer(Label timerLabel) {
	    if (timerLabel == null) return;
	    this.activeCallTimerLabel = timerLabel;
	    this.callStartTimeMillis = System.currentTimeMillis();

	    if (callTimer != null) {
	        callTimer.stop();
	    }

	    callTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
	        if (!isInCall || callStartTimeMillis == 0) {
	            callTimer.stop();
	            return;
	        }
	        long elapsedMillis = System.currentTimeMillis() - callStartTimeMillis;
	        long elapsedSeconds = elapsedMillis / 1000;
	        long secondsDisplay = elapsedSeconds % 60;
	        long minutesDisplay = (elapsedSeconds % 3600) / 60;
	        long hoursDisplay = elapsedSeconds / 3600;

	        String timeString;
	        if (hoursDisplay > 0) {
	            timeString = String.format("%d:%02d:%02d", hoursDisplay, minutesDisplay, secondsDisplay);
	        } else {
	            timeString = String.format("%02d:%02d", minutesDisplay, secondsDisplay);
	        }
	        
	        if (activeCallTimerLabel != null) {
	            activeCallTimerLabel.setText(timeString);
	        }
	    }));
	    callTimer.setCycleCount(Timeline.INDEFINITE);
	    callTimer.play();
	}

	// Ajouter cette méthode utilitaire
	private String getDisplayNameForUser(String username) {
	    for (ClientDisplayWrapper w : myPersonalContactsList) {
	        if (w.getClient().getNomUtilisateur().equals(username)) {
	            return w.getDisplayName();
	        }
	    }
	    return username;
	}

	public void initiateP2PConnection(String peerUsername, String callId, String peerIp, int peerP2PPort, String type) {
	    if (!"audio".equals(type)) { // Cette méthode ne gère plus que les appels audio simples
	        System.err.println("initiateP2PConnection appelée pour un type non-audio. Logique déplacée.");
	        return;
	    }
	    this.isInCall = true;
	    updateStatus("Appel audio accepté. Connexion P2P à " + peerUsername + "...");
	    String peerDisplayName = getDisplayNameForUser(peerUsername);

	    new Thread(() -> {
	        try {
	            p2pAudioSocket = new Socket(peerIp, peerP2PPort);
	            p2pAudioOut = new ObjectOutputStream(p2pAudioSocket.getOutputStream());
	            p2pAudioOut.flush();
	            p2pAudioIn = new ObjectInputStream(p2pAudioSocket.getInputStream());

	            Platform.runLater(() -> {
	                showAudioCallWindow(peerDisplayName, true);
	                startCallTimer(audioCallController.getTimerLabel());
	            });

	            // Démarrer l'envoi ET la réception pour l'appel audio
	            startAudioStreaming();
	            startAudioReception(); // <-- LA LOGIQUE DE RÉCEPTION MANQUANTE

	        } catch (IOException e) {
	            e.printStackTrace();
	            Platform.runLater(() -> {
	                showAlert(AlertType.ERROR, "Erreur P2P", "Impossible de se connecter en P2P à " + peerDisplayName);
	                endCallLogic(true);
	            });
	        }
	    }).start();
	}

	public void handleCallAcceptedByPeer(String responderUsername, String callId, String peerIp, int peerP2PPort,
			String type) {
		if (this.activeCallId == null || !this.activeCallId.equals(callId)) {
			System.err.println("Réponse d'acceptation reçue pour un appel inconnu ou inactif. ID d'appel attendu: "
					+ this.activeCallId + ", reçu: " + callId);
			return;
		}
		initiateP2PConnection(responderUsername, callId, peerIp, peerP2PPort, type);
	}

	public void handleCallRejectedByPeer(String responderUsername, String callId, String type) {
		if (this.activeCallId == null || !this.activeCallId.equals(callId)) {
			return;
		}

		String responderDisplayName = responderUsername;
		for (ClientDisplayWrapper w : myPersonalContactsList) {
			if (w.getClient().getNomUtilisateur().equals(responderUsername)) {
				responderDisplayName = w.getDisplayName();
				break;
			}
		}

		final String finalResponderDisplayName = responderDisplayName;
		Platform.runLater(() -> {
			showAlert(AlertType.INFORMATION, "Appel " + type + " Refusé",
					finalResponderDisplayName + " a refusé votre appel.");
			displayCallStatusMessage("Appel " + type + " à " + finalResponderDisplayName + " refusé par le pair.",
					"rejected", type);
		});
		saveCallEventToDatabase(currentUser.getId(), getContactIdByUsername(responderUsername), activeCallId,
				type + "_rejected_outgoing");
		resetCallState();
	}

	public void handleCallEndedByPeer(String enderUsername, String callId, String type) {
		if (this.activeCallId == null || !this.activeCallId.equals(callId) || !isInCall) {
			return;
		}

		String enderDisplayName = enderUsername;
		for (ClientDisplayWrapper w : myPersonalContactsList) {
			if (w.getClient().getNomUtilisateur().equals(enderUsername)) {
				enderDisplayName = w.getDisplayName();
				break;
			}
		}
		final String finalEnderDisplayName = enderDisplayName;

		Platform.runLater(() -> {
			showAlert(AlertType.INFORMATION, "Appel " + type + " Terminé",
					finalEnderDisplayName + " a terminé l'appel.");
			displayCallStatusMessage("Appel " + type + " avec " + finalEnderDisplayName + " terminé par le pair.",
					"ended", type);
		});
		saveCallEventToDatabase(currentUser.getId(), getContactIdByUsername(enderUsername), activeCallId,
				type + "_ended_by_peer");
		closeCallUIAndStreams();
		resetCallState();
	}

	private int startP2PServerForCall(String type) {
	    if (!"audio".equals(type)) return -1; // Ne gère que l'audio
	    try {
	        if (p2pAudioServerSocket != null && !p2pAudioServerSocket.isClosed()) p2pAudioServerSocket.close();
	        
	        p2pAudioServerSocket = new ServerSocket(0);
	        int port = p2pAudioServerSocket.getLocalPort();
	        System.out.println("Serveur P2P audio (appel entrant) démarré sur le port: " + port);

	        p2pAudioThread = new Thread(() -> {
	            try {
	                p2pAudioSocket = p2pAudioServerSocket.accept();
	                p2pAudioOut = new ObjectOutputStream(p2pAudioSocket.getOutputStream());
	                p2pAudioOut.flush();
	                p2pAudioIn = new ObjectInputStream(p2pAudioSocket.getInputStream());
	                
	                System.out.println("Connexion P2P audio entrante acceptée de: " + p2pAudioSocket.getInetAddress());

	                // Démarrer l'envoi ET la réception
	                startAudioStreaming();
	                startAudioReception(); // <-- LA LOGIQUE DE RÉCEPTION MANQUANTE
	                
	            } catch (IOException e) {
	                if (isInCall) endCallLogic(false);
	            }
	        });
	        p2pAudioThread.setDaemon(true);
	        p2pAudioThread.start();
	        return port;
	    } catch (IOException e) {
	        e.printStackTrace();
	        return -1;
	    }
	}



	private void showAudioCallWindow(String contactName, boolean isInitiator) {
		try {
			if (audioCallStage != null && audioCallStage.isShowing()) {
				audioCallStage.toFront();
				return;
			}
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/Alanya/audio_call_view.fxml"));
			Parent root = loader.load();
			audioCallController = loader.getController();

			audioCallStage = new Stage();
			audioCallStage.setTitle("Appel Audio - " + contactName);
			audioCallStage.setScene(new Scene(root));
			audioCallStage.initModality(Modality.NONE);
			Node sourceNode = contactListView;
			if (sourceNode != null && sourceNode.getScene() != null && sourceNode.getScene().getWindow() != null) {
				audioCallStage.initOwner(sourceNode.getScene().getWindow());
			}

			if (audioCallController != null) {
				audioCallController.initializeCall(this, contactName, activeCallId, audioCallStage);
			} else {
				System.err.println("AudioCallController est null après chargement de audio_call_view.fxml");
				Button hangUpButton = (Button) root.lookup("#hangUpButtonAudio");
				if (hangUpButton != null) {
					hangUpButton.setOnAction(e -> endCallLogic(true));
				}
				Label contactNameLabelAudio = (Label) root.lookup("#contactNameLabelAudio");
				if (contactNameLabelAudio != null)
					contactNameLabelAudio.setText(contactName);
			}

			audioCallStage.setOnCloseRequest(event -> {
				event.consume();
				endCallLogic(true);
			});
			audioCallStage.show();
		} catch (IOException e) {
			e.printStackTrace();
			showAlert(AlertType.ERROR, "Erreur UI", "Impossible d'ouvrir l'interface d'appel audio: " + e.getMessage());
		}
	}

	private void showVideoCallWindow(String contactName, boolean isInitiator) {
		try {
			if (videoCallStage != null && videoCallStage.isShowing()) {
				videoCallStage.toFront();
				return;
			}
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/Alanya/video_call_view.fxml"));
			Parent root = loader.load();
			videoCallController = loader.getController();

			videoCallStage = new Stage();
			videoCallStage.setTitle("Appel Vidéo - " + contactName);
			videoCallStage.setScene(new Scene(root));
			videoCallStage.initModality(Modality.NONE);
			Node sourceNode = contactListView;
			if (sourceNode != null && sourceNode.getScene() != null && sourceNode.getScene().getWindow() != null) {
				videoCallStage.initOwner(sourceNode.getScene().getWindow());
			}

			if (videoCallController != null) {
				videoCallController.initializeCall(this, contactName, activeCallId, videoCallStage);
				this.localVideoView = videoCallController.getLocalImageView();
				this.remoteVideoView = videoCallController.getRemoteImageView();
			} else {
				System.err.println("VideoCallController est null après chargement de video_call_view.fxml");
				this.localVideoView = (ImageView) root.lookup("#localVideoView");
				this.remoteVideoView = (ImageView) root.lookup("#remoteVideoView");
				Button hangUpButton = (Button) root.lookup("#hangUpButtonVideo");
				ToggleButton muteButton = (ToggleButton) root.lookup("#muteButtonVideo");
				if (hangUpButton != null)
					hangUpButton.setOnAction(e -> endCallLogic(true));
				if (muteButton != null)
					muteButton.setOnAction(e -> toggleMute(muteButton.isSelected()));
			}

			videoCallStage.setOnCloseRequest(event -> {
				event.consume();
				endCallLogic(true);
			});
			videoCallStage.show();
		} catch (IOException e) {
			e.printStackTrace();
			showAlert(AlertType.ERROR, "Erreur UI", "Impossible d'ouvrir l'interface d'appel vidéo: " + e.getMessage());
		}
	}

	void endCallLogic(boolean notifyPeer) {
	    if (!isInCall && callStartTimeMillis == 0) {
	        closeCallUIAndStreams();
	        return;
	    }

	    // Capturer l'état avant de le réinitialiser
	    final String callIdToEnd = this.activeCallId;
	    final String partnerToEndWith = this.activeCallPartner;
	    final String typeOfCallEnded = this.callType;

	    // Arrêter le minuteur et calculer la durée
	    if (callTimer != null) {
	        callTimer.stop();
	    }
	    long durationSeconds = 0;
	    if (callStartTimeMillis > 0) {
	        durationSeconds = (System.currentTimeMillis() - callStartTimeMillis) / 1000;
	    }
	    long minutes = durationSeconds / 60;
	    long seconds = durationSeconds % 60;
	    String formattedDuration = String.format("%02d:%02d", minutes, seconds);

	    // Réinitialiser l'état immédiatement
	    this.isInCall = false;
	    this.audioStreaming = false;
	    this.videoStreaming = false;
	    this.callStartTimeMillis = 0;
	    
	    // Notifier le pair
	    if (notifyPeer && centralServerConnection != null && centralServerConnection.isConnected() && partnerToEndWith != null && callIdToEnd != null) {
	        Map<String, String> callData = new HashMap<>();
	        callData.put("callId", callIdToEnd);
	        callData.put("targetUsername", partnerToEndWith);
	        callData.put("type", typeOfCallEnded);
	        centralServerConnection.sendCommand(new ServerCommand(ServerCommand.ServerCommandType.END_CALL, callData));
	    }

	    // NOUVELLE LOGIQUE : Préparer le message final et le sauvegarder
	    String endMessageContent = "APPEL TERMINÉ : Durée : " + formattedDuration;
	    
	    // Afficher le message de fin dans le chat en temps réel
	    displayCallStatusMessage(endMessageContent, "ended", typeOfCallEnded);
	    
	    // Sauvegarder l'événement exact dans la BDD
	    if (currentUser != null && partnerToEndWith != null) {
	        saveCallEventToDatabase(currentUser.getId(), getContactIdByUsername(partnerToEndWith), callIdToEnd, endMessageContent);
	    }

	    // Nettoyer les ressources et l'UI
	    closeCallUIAndStreams();
	    resetCallState();
	    System.out.println("endCallLogic: Terminé.");
	}

	private void closeCallUIAndStreams() {
		System.out.println("closeCallUIAndStreams: Début de la fermeture des UI et flux d'appel.");
		if (microphone != null) {
			if (microphone.isRunning())
				microphone.stop();
			if (microphone.isOpen())
				microphone.close();
			microphone = null;
			System.out.println("Microphone fermé.");
		}
		if (speakers != null) {
			speakers.drain();
			if (speakers.isRunning())
				speakers.stop();
			if (speakers.isOpen())
				speakers.close();
			speakers = null;
			System.out.println("Haut-parleurs fermés.");
		}
		if (videoCapture != null && videoCapture.isOpened()) {
			videoCapture.release();
			videoCapture = null;
			System.out.println("Capture vidéo relâchée.");
		}

		try {
			if (p2pAudioOut != null) {
				p2pAudioOut.close();
				System.out.println("p2pAudioOut fermé.");
			}
			if (p2pAudioIn != null) {
				p2pAudioIn.close();
				System.out.println("p2pAudioIn fermé.");
			}
			if (p2pAudioSocket != null && !p2pAudioSocket.isClosed()) {
				p2pAudioSocket.close();
				System.out.println("p2pAudioSocket fermé.");
			}
			if (p2pAudioServerSocket != null && !p2pAudioServerSocket.isClosed()) {
				p2pAudioServerSocket.close();
				System.out.println("p2pAudioServerSocket fermé.");
			}

			if (p2pVideoOut != null) {
				p2pVideoOut.close();
				System.out.println("p2pVideoOut fermé.");
			}
			if (p2pVideoIn != null) {
				p2pVideoIn.close();
				System.out.println("p2pVideoIn fermé.");
			}
			if (p2pVideoSocket != null && !p2pVideoSocket.isClosed()) {
				p2pVideoSocket.close();
				System.out.println("p2pVideoSocket fermé.");
			}
			if (p2pVideoServerSocket != null && !p2pVideoServerSocket.isClosed()) {
				p2pVideoServerSocket.close();
				System.out.println("p2pVideoServerSocket fermé.");
			}
		} catch (IOException e) {
			System.err.println("Erreur lors de la fermeture des flux/sockets P2P d'appel: " + e.getMessage());
		} finally {
			p2pAudioOut = null;
			p2pAudioIn = null;
			p2pAudioSocket = null;
			p2pAudioServerSocket = null;
			p2pVideoOut = null;
			p2pVideoIn = null;
			p2pVideoSocket = null;
			p2pVideoServerSocket = null;
		}
		System.out.println("Tous les flux et sockets P2P d'appel devraient être fermés et nullifiés.");

		Platform.runLater(() -> {
			if (audioCallStage != null && audioCallStage.isShowing()) {
				audioCallStage.close();
				System.out.println("Fenêtre d'appel audio fermée.");
			}
			audioCallStage = null;
			audioCallController = null;

			if (videoCallStage != null && videoCallStage.isShowing()) {
				videoCallStage.close();
				System.out.println("Fenêtre d'appel vidéo fermée.");
			}
			videoCallStage = null;
			videoCallController = null;
			localVideoView = null;
			remoteVideoView = null;
		});
		System.out.println("closeCallUIAndStreams: Interfaces d'appel et flux P2P fermés.");
	}

	private void resetCallState() {
	    isInCall = false; // <-- LA LIGNE MANQUANTE QUI CORRIGE LE BUG
	    activeCallId = null;
	    activeCallPartner = null;
	    callType = null;
	    updateStatus("Prêt.");
	    System.out.println("resetCallState: État de l'appel réinitialisé.");
	}
	

	
	
	static class AudioPacket implements java.io.Serializable {
		private static final long serialVersionUID = 1L;
		private byte[] data;
		private int length;

		public AudioPacket(byte[] data, int length) {
			this.data = new byte[length];
			System.arraycopy(data, 0, this.data, 0, length);
			this.length = length;
		}

		public byte[] getData() {
			return data;
		}

		public int getLength() {
			return length;
		}
	}

	// Remplacer cette méthode dans Mainfirstclientcontroller.java

	// Remplacer cette méthode dans Mainfirstclientcontroller.java
	// Remplacer cette méthode dans Mainfirstclientcontroller.java

	private void startAudioReception() {
	    if (p2pAudioIn == null) {
	        System.err.println("AUDIO_RECV: Flux d'entrée audio est null. Impossible de recevoir.");
	        return;
	    }
	    new Thread(() -> {
	        try {
	            if (speakers == null || !speakers.isOpen()) {
	                speakers = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, STANDARD_AUDIO_FORMAT));
	                
	                int bufferSize = 3500; 
	                speakers.open(STANDARD_AUDIO_FORMAT, bufferSize); 
	                
	                speakers.start();
	                System.out.println("AUDIO_RECV: Haut-parleurs ouverts avec un buffer de " + bufferSize + " bytes.");
	            }
	            while (isInCall) {
	                Object received = p2pAudioIn.readObject();
	                if (received instanceof AudioPacket) {
	                    AudioPacket packet = (AudioPacket) received;
	                    if (speakers != null) {
	                        speakers.write(packet.getData(), 0, packet.getLength());
	                    }
	                }
	            }
	        } catch (Exception e) {
	            if (isInCall) {
	                System.out.println("AUDIO_RECV: Connexion audio terminée ou rompue: " + e.getMessage());
	            }
	        }
	    }, "AudioReceiveThread").start();
	}

		static class VideoPacket implements java.io.Serializable {
		private static final long serialVersionUID = 2L;
		private byte[] frameData;

		public VideoPacket(byte[] frameData) {
			this.frameData = frameData;
		}

		public byte[] getFrameData() {
			return frameData;
		}
	}

	
		
		// Remplacer cette méthode

	void toggleMute(boolean muted) {
		    if (!isInCall) return;

		    // On change simplement l'état de la variable. Le thread d'envoi réagira à ce changement.
		    audioStreaming = !muted;

		    System.out.println("Audio " + (muted ? "coupé" : "réactivé") + ". audioStreaming=" + audioStreaming);

		    // Mettre à jour l'interface utilisateur du bouton dans la fenêtre d'appel
		    if (videoCallController != null && "video".equals(callType)) {
		        videoCallController.updateMuteButtonState(muted);
		    }
		}

	private void startVideoStreaming() {
	    if (!isInCall || !"video".equals(callType)) {
	        return;
	    }
	    
	    final ObjectOutputStream outStreamForVideo = p2pVideoOut;
	    
	    if (outStreamForVideo == null) {
	        System.err.println("VIDEO_STREAM: Flux de sortie P2P vidéo (p2pVideoOut) est null. L'envoi ne peut pas commencer.");
	        return;
	    }
	    
	    // CORRECTION : Gestion d'erreur pour la bibliothèque OpenCV
	    try {
	    	Loader.load(opencv_java.class);
	    } catch (UnsatisfiedLinkError e) {
	        System.err.println("VID_STREAM: Impossible de charger la bibliothèque native OpenCV: " + e.getMessage());
	        Platform.runLater(() -> {
	            showAlert(AlertType.ERROR, "Erreur Vidéo Critique", "La bibliothèque OpenCV est manquante ou mal configurée. Assurez-vous qu'elle est installée et que le chemin vers les librairies natives est correct.");
	            endCallLogic(true);
	        });
	        return;
	    }
	    videoStreaming = true;

	    if (videoCapture == null || !videoCapture.isOpened()) {
	        try {
	            videoCapture = new VideoCapture(0); // Tente d'ouvrir la caméra par défaut
	            if (!videoCapture.isOpened()) {
	                // CORRECTION : Alerte spécifique pour la caméra
	                System.err.println("VID_STREAM: Échec de l'ouverture de la caméra.");
	                Platform.runLater(() -> {
	                    showAlert(AlertType.ERROR, "Erreur Caméra", "Impossible d'accéder à la caméra. Vérifiez qu'elle est bien branchée, fonctionnelle et non utilisée par une autre application.");
	                    endCallLogic(true);
	                });
	                videoStreaming = false;
	                return;
	            }
	            Thread.sleep(500); // Laisse le temps à la caméra de s'initialiser
	        } catch (Exception e) {
	            System.err.println("VID_STREAM: Exception lors de l'initialisation de VideoCapture: " + e.getMessage());
	            Platform.runLater(() -> {
	                showAlert(AlertType.ERROR, "Erreur Caméra", "Une erreur est survenue lors de l'initialisation de la caméra: " + e.getMessage());
	                endCallLogic(true);
	            });
	            videoStreaming = false;
	            return;
	        }
	    }

	    // Le reste de la méthode (threads d'envoi et de réception) reste le même...
	    if (p2pVideoOut != null) {
	        new Thread(() -> {
	            Mat frame = new Mat();
	            MatOfByte mob = new MatOfByte();
	            try {
	                while (isInCall && videoStreaming) {
	                    if (videoCapture == null || !videoCapture.isOpened() || !videoCapture.read(frame) || frame.empty()) {
	                         System.err.println("VID_SEND: Échec de lecture de la frame. Arrêt du thread.");
	                         Platform.runLater(() -> {
	                            showAlert(AlertType.WARNING, "Erreur Caméra", "La connexion avec la caméra a été perdue.");
	                            endCallLogic(true);
	                         });
	                         break;
	                    }

	                    if (localVideoView != null) {
	                        Image fxImage = matToFxImage(frame);
	                        Platform.runLater(() -> localVideoView.setImage(fxImage));
	                    }
	                    Imgcodecs.imencode(".jpg", frame, mob);
	                    VideoPacket packet = new VideoPacket(mob.toArray());
	                    synchronized (outStreamForVideo) {
	                        if (isInCall && videoStreaming) {
	                        	outStreamForVideo.writeObject(packet);
	                        	outStreamForVideo.flush();
	                        	outStreamForVideo.reset();
	                        } else break;
	                    }
	                    Thread.sleep(40);
	                }
	            } catch (Exception e) {
	                if (isInCall) {
	                    System.err.println("VID_SEND: Erreur dans le thread d'envoi vidéo P2P: " + e.getMessage());
	                }
	            } finally {
	                if (frame != null) frame.release();
	                if (mob != null) mob.release();
	            }
	        }, "VideoSendThread").start();
	    } else {
	        System.err.println("VID_STREAM: Impossible de démarrer l'envoi vidéo, p2pVideoOut est null.");
	        Platform.runLater(() -> endCallLogic(true));
	    }
	}
	
	// Remplacer toute la méthode startAudioStreaming par celle-ci
	private void startAudioStreaming() {
	    if (!isInCall) return;
	    audioStreaming = true; // Assure que le son est activé par défaut au début de l'appel

	    final ObjectOutputStream outStreamForAudio = p2pAudioOut;
	    
	    if (outStreamForAudio == null) {
	        System.err.println("AUDIO_STREAM: Flux de sortie P2P audio (p2pAudioOut) est null. L'envoi ne peut pas commencer.");
	        return;
	    }

	    Thread audioSendThread = new Thread(() -> {
	        try {
	            AudioFormat format = STANDARD_AUDIO_FORMAT;
	            if (microphone == null || !microphone.isOpen()) {
	                // Gestion d'erreur améliorée pour l'ouverture du microphone
	                try {
	                    microphone = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, format));
	                    // Utilisation d'une taille de buffer sûre et compatible
	                    microphone.open(format, 4096); 
	                } catch (LineUnavailableException e) {
	                    Platform.runLater(() -> {
	                        showAlert(AlertType.ERROR, "Erreur Microphone", "Impossible d'ouvrir le microphone. Vérifiez qu'il est bien branché et non utilisé par une autre application.\n\nErreur: " + e.getMessage());
	                        endCallLogic(true);
	                    });
	                    return; // Arrête le thread si le micro est indisponible
	                }
	            }
	            if (!microphone.isRunning()) {
	                microphone.start();
	            }

	            byte[] buffer = new byte[300];
	            
	            // CORRECTION : La boucle principale continue tant que l'appel est actif
	            while (isInCall) {
	                // Le thread vérifie à chaque tour de boucle si le son est activé
	                if (audioStreaming) {
	                    int bytesRead = microphone.read(buffer, 0, buffer.length);
	                    if (bytesRead > 0) {
	                        synchronized (outStreamForAudio) {
	                             if (isInCall) { // Double vérification en cas d'arrêt pendant le blocage
	                                 outStreamForAudio.writeObject(new AudioPacket(buffer, bytesRead));
	                                 outStreamForAudio.flush();
	                                 outStreamForAudio.reset();
	                             }
	                        }
	                    }
	                } else {
	                    // Si le son est coupé, le thread fait une petite pause pour ne pas surcharger le CPU
	                    Thread.sleep(20); 
	                }
	            }
	        } catch (Exception e) {
	            if (isInCall) {
	                System.err.println("AUDIO_SEND: Erreur dans le thread d'envoi audio: " + e.getMessage());
	            }
	        }
	    }, "AudioSendThread");
	    
	    audioSendThread.setDaemon(true);
	    audioSendThread.start();
	}
	
	
	private void startVideoReception() {
	    if (p2pVideoIn == null) {
	        System.err.println("VIDEO_RECV: Flux d'entrée vidéo est null. Impossible de recevoir.");
	        return;
	    }
	    
	    Thread videoThread = new Thread(() -> {
	        try {
	            while (isInCall) {
	                Object received = p2pVideoIn.readObject();
	                if (received instanceof VideoPacket) {
	                    VideoPacket packet = (VideoPacket) received;
	                    if (remoteVideoView != null) {
	                        // Ce processus est gourmand, d'où l'importance de la priorité du thread
	                        Image fxImage = new Image(new ByteArrayInputStream(packet.getFrameData()));
	                        Platform.runLater(() -> remoteVideoView.setImage(fxImage));
	                    }
	                }
	            }
	        } catch (Exception e) {
	            if (isInCall) {
	                System.out.println("VIDEO_RECV: Connexion vidéo terminée ou rompue: " + e.getMessage());
	                Platform.runLater(() -> endCallLogic(false));
	            }
	        }
	    }, "VideoReceiveThread");

	    // OPTIMISATION : Donner une priorité plus élevée au thread vidéo pour un affichage plus fluide
	    videoThread.setPriority(Thread.MAX_PRIORITY - 2);
	    videoThread.setDaemon(true);
	    videoThread.start();
	}
	private Image matToFxImage(Mat mat) {
		if (mat == null || mat.empty()) {
			return null;
		}
		MatOfByte buffer = new MatOfByte();
		Imgcodecs.imencode(".png", mat, buffer);
		return new Image(new ByteArrayInputStream(buffer.toArray()));
	}

	private void saveCallEventToDatabase(int userId1, int userId2, String callIdentifier, String contentToSave) {
	    if (userId1 <= 0 || userId2 <= 0) {
	        System.err.println("Sauvegarde log d'appel: ID utilisateur invalide.");
	        return;
	    }

	    // Obtenir les noms d'utilisateur pour la BDD
	    String username1 = "Utilisateur " + userId1;
	    String username2 = "Utilisateur " + userId2;
	    try (Connection conn = DatabaseConnection.getConnection()) {
	        if (conn != null) {
	            username1 = Client.getUsernameById(conn, userId1);
	            username2 = Client.getUsernameById(conn, userId2);
	        }
	    } catch (SQLException e) {
	        System.err.println("Erreur SQL lors de la récupération des noms d'utilisateur: " + e.getMessage());
	    }

	    // Créer un objet Message avec le contenu exact à sauvegarder
	    Message callEventMessage = new Message(
	        (username1 != null ? username1 : "Utilisateur " + userId1),
	        (username2 != null ? username2 : "Utilisateur " + userId2),
	        contentToSave, // Utilise directement le contenu formaté
	        java.time.LocalDateTime.now().format(Message.formatter)
	    );

	    // Sauvegarder dans la base de données
	    storeSentMessageInDB(callEventMessage, userId1, userId2);
	}

	private int getContactIdByUsername(String username) {
		if (username == null || username.isEmpty())
			return -1;
		for (ClientDisplayWrapper wrapper : myPersonalContactsList) {
			if (wrapper != null && wrapper.getClient() != null
					&& wrapper.getClient().getNomUtilisateur().equals(username)) {
				return wrapper.getClient().getId();
			}
		}
		Client c = findClientInSystemByUsername(username);
		if (c != null) {
			return c.getId();
		} else {
			System.err.println("Impossible de trouver l'ID pour le contact: " + username + " (getContactIdByUsername)");
			return -1;
		}
	}

	@FXML
    void handleMenu(ActionEvent event) {
        // Cette méthode affiche le menu que nous venons de reconfigurer
        Node sourceButton = (Node) event.getSource();
        chatContextMenu.show(sourceButton, Side.BOTTOM, 0, 0);
    }


	public void handleFileRequestFromPeer(String requesterUsername, FileRequestMessage request) {
		System.out.println("MFC: Reçu une demande de fichier de " + requesterUsername + " pour "
				+ request.getFileNameToGet() + ", chemin original chez moi: " + request.getSenderOriginalFilePath());

		File fileToSend = new File(request.getSenderOriginalFilePath());

		PeerSession sessionWithRequester = activePeerSessions.get(requesterUsername);
		if (sessionWithRequester == null || !sessionWithRequester.isConnected()) {
			System.err.println("MFC: Session P2P avec le demandeur " + requesterUsername
					+ " non active. Impossible d'envoyer le fichier.");
			return;
		}

		if (fileToSend.exists() && fileToSend.isFile()) {
			System.out.println("MFC: Début de l'envoi du fichier " + fileToSend.getName() + " à " + requesterUsername);
			sessionWithRequester.sendFileInChunks(fileToSend, request.getFileNameToGet());
		} else {
			System.err.println("MFC: Fichier demandé '" + request.getSenderOriginalFilePath()
					+ "' non trouvé sur le système de l'expéditeur.");
			sessionWithRequester
					.sendP2PObject(new FileTransferStatusMessage(request.getFileNameToGet(), "ERROR_FILE_NOT_FOUND"));
		}
	}

	// Modifié pour stocker le callback et le chemin de sauvegarde final
	public void initiateP2PFileDownloadRequest(String originalFileSenderUsername, long messageDbId, // AJOUTÉ : ID du
																									// message pour la
																									// clé unique
			AttachmentInfo attachmentToDownload, String chosenLocalSavePath,
			BiConsumer<Boolean, File> onCompleteCallbackFromAttachmentService) {

		System.out.println("MFC: Demande de téléchargement P2P à " + originalFileSenderUsername + " pour fichier "
				+ attachmentToDownload.getFileName() + " (msgID: " + messageDbId + ")" + " (chemin original exp: "
				+ attachmentToDownload.getLocalPath() + "), sauvegarde vers " + chosenLocalSavePath);

		PeerSession peerSession = activePeerSessions.get(originalFileSenderUsername);

		if (peerSession != null && peerSession.isConnected()) {
			String uniqueFileKey = messageDbId + "!" + attachmentToDownload.getFileName(); // Clé standardisée

			// Stocker le contexte du téléchargement
			expectedFileDownloadSizes.put(uniqueFileKey, attachmentToDownload.getFileSize());
			activeDownloadFinalSavePaths.put(uniqueFileKey, chosenLocalSavePath);
			activeDownloadCallbacks.put(uniqueFileKey, onCompleteCallbackFromAttachmentService);

			FileRequestMessage request = new FileRequestMessage(attachmentToDownload.getFileName(),
					attachmentToDownload.getLocalPath(), currentUser.getNomUtilisateur(), originalFileSenderUsername);
			peerSession.sendP2PObject(request);

			updateFileProgress(uniqueFileKey, 0.001); // Indiquer "en attente" et créer la prop si besoin

		} else {
			showAlert(AlertType.ERROR, "Erreur Connexion P2P",
					"Impossible de se connecter à " + originalFileSenderUsername + " pour télécharger le fichier.");
			if (onCompleteCallbackFromAttachmentService != null) {
				onCompleteCallbackFromAttachmentService.accept(false, null);
			}
			// Nettoyer le contexte si la connexion P2P échoue immédiatement
			String uniqueFileKey = messageDbId + "!" + attachmentToDownload.getFileName();
			cleanupFailedDownload(uniqueFileKey, Paths.get(
					chosenLocalSavePath + ".part_" + originalFileSenderUsername.replaceAll("[^a-zA-Z0-9.-]", "_")));

		}
	}
	
	public void reloadEmojis() {
        loadAvailableEmojis();
        setupEmojiGrid();
        statusLabel.setText("Emojis colorés rechargés: " + availableEmojis.size());
    }

    /**
     * Ajoute un nouvel emoji avec son fichier image
     */
    public void addCustomEmoji(String unicode, String fileName, String description) {
        emojiToFileMap.put(unicode, fileName);
        availableEmojis.add(new EmojiData(unicode, fileName, description));
        setupEmojiGrid();
    }

}