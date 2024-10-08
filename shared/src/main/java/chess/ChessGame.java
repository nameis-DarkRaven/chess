package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor teamTurn;
    private ChessBoard board;

    public ChessGame() {
        teamTurn = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validOnes = new ArrayList<>();
        for (ChessMove move : moves) {
            if (!willBeInCheck(piece.getTeamColor(), move)) {
                validOnes.add(move);
            }
        }
        return validOnes;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        if (board.getPieces()[8 - start.getRow()][start.getColumn() - 1] != null &&
                validMoves(start).contains(move) &&
                board.getPiece(start).getTeamColor() == teamTurn) {
            if (move.getPromotionPiece() == null) {
                board.addPiece(move.getEndPosition(), board.getPiece(start));
                board.addPiece(start, null);
            } else {
                board.addPiece(move.getEndPosition(), new ChessPiece(this.teamTurn, move.getPromotionPiece()));
                board.addPiece(start, null);
            }
            if (teamTurn == TeamColor.WHITE) {
                setTeamTurn(TeamColor.BLACK);
            } else {
                setTeamTurn(TeamColor.WHITE);
            }
        } else {
            throw new InvalidMoveException();
        }
    }

    private Collection<ChessMove> getPossibleMoves(TeamColor teamColor) {
        Collection<ChessMove> moves = new ArrayList<>();
        for (int i = 1; i < board.getPieces().length + 1; i++) {
            for (int j = 1; j < board.getPieces().length + 1; j++) {
                if (board.getPieces()[8 - i][j - 1] != null) {
                    ChessPosition position = new ChessPosition(i, j);
                    if (board.getPiece(position).getTeamColor() == teamColor) {
                        moves.addAll(board.getPiece(position).pieceMoves(board, position));
                    }
                }
            }
        }
        return moves;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        for (int i = 1; i < board.getPieces().length + 1; i++) {
            for (int j = 1; j < board.getPieces().length + 1; j++) {
                if (board.getPieces()[8 - i][j - 1] != null && board.getPieces()[8 - i][j - 1].getTeamColor() != teamColor) {
                    ChessPosition position = new ChessPosition(i, j);
                    ChessPiece piece = board.getPiece(position);
                    possibleMoves.addAll(piece.pieceMoves(board, position));
                }
            }
            for (ChessMove move : possibleMoves) {
                ChessPiece piece = board.getPiece(move.getEndPosition());
                if (board.getPieces()[8 - move.getEndPosition().getRow()][move.getEndPosition().getColumn() - 1] != null && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    for (int k = 1; k < board.getPieces().length + 1; k++) {
                        for (int j = 1; j < board.getPieces().length + 1; j++) {
                            if (board.getPieces()[8 - k][j - 1] != null) {
                                ChessPosition position = new ChessPosition(k, j);
                                if (board.getPiece(position).getPieceType() == ChessPiece.PieceType.KING && board.getPiece(position).getTeamColor() == teamColor) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean willBeInCheck(TeamColor teamColor, ChessMove move) {
        ChessPiece temp = board.getPiece(move.getEndPosition());
        board.addPiece(move.getEndPosition(), board.getPiece(move.getStartPosition()));
        board.addPiece(move.getStartPosition(), null);

        if (isInCheck(teamColor)) {
            ChessMove backMove = new ChessMove(move.getEndPosition(), move.getStartPosition(), move.getPromotionPiece());
            board.addPiece(backMove.getEndPosition(), board.getPiece(backMove.getStartPosition()));
            board.addPiece(backMove.getStartPosition(), temp);
            return true;
        }

        ChessMove backMove = new ChessMove(move.getEndPosition(), move.getStartPosition(), move.getPromotionPiece());
        board.addPiece(backMove.getEndPosition(), board.getPiece(backMove.getStartPosition()));
        board.addPiece(backMove.getStartPosition(), temp);
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        boolean isStalemate = false;
        Collection<ChessMove> moves = getPossibleMoves(teamColor);
        for (ChessMove move : moves) {
            if (!willBeInCheck(teamColor, move)) {
                return false;
            } else {
                isStalemate = true;
            }
        }
        return isStalemate;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */

    public boolean isInCheckmate(TeamColor teamColor) {
        boolean isCheckmate = false;
        Collection<ChessMove> moves = getPossibleMoves(teamColor);
        if (isInCheck(teamColor)) {
            for (ChessMove move : moves) {
                if (!willBeInCheck(teamColor, move)) {
                    return false;
                }
            }
            isCheckmate = true;
        }
        return isCheckmate;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "teamTurn=" + teamTurn +
                ", board=" + board +
                '}';
    }
}
